"""Render existing CI evidence as Markdown, without running or changing quality gates."""

import argparse
from collections import Counter
import html
import json
import os
from pathlib import Path
import re
from urllib.parse import urlencode
import xml.etree.ElementTree as ET


PUBLISHED_MODULES = (
    "mcp-core",
    "mcp-transport-http",
    "mcp-transport-stdio",
    "mcp-spring-webmvc",
    "mcp-spring-webflux",
    "mcp-javalin",
)
TEST_MODULES = PUBLISHED_MODULES + ("conformance",) + tuple(
    f"examples/{name}"
    for name in ("spring-webmvc", "spring-webflux", "javalin", "quarkus", "micronaut")
)


def safe_text(value):
    # Render report data as text, not contributor-controlled Markdown or HTML.
    first_line = (str(value).splitlines() or [""])[0]
    text = "".join(char for char in first_line if char.isprintable())
    text = text[:400] + ("…" if len(text) > 400 else "")
    return re.sub(r"[\\`*_\[\]()|!]", lambda match: f"&#{ord(match[0])};", html.escape(text))


def xml_report(root, path, tag, problems):
    try:
        report = ET.parse(root / path).getroot()
        if report.tag != tag:
            raise ValueError("Unexpected report type")
        return report
    except FileNotFoundError:
        problems.append(f"Missing: {path}")
    except (OSError, ET.ParseError, ValueError):
        problems.append(f"Malformed: {path}")
    return None


def count(node, attribute):
    if node is None or attribute not in node.attrib:
        raise ValueError("Missing counter")
    value = int(node.attrib[attribute])
    if value < 0:
        raise ValueError("Negative counter")
    return value


def coverage_rows(root, problems):
    rows = [
        "### Line coverage", "", "| Module | Coverage | Required |",
        "| --- | --- | --- |",
    ]
    for module in PUBLISHED_MODULES:
        path = Path(module) / "build/reports/jacoco/test/jacocoTestReport.xml"
        value = "Unavailable"
        report = xml_report(root, path, "report", problems)
        if report is not None:
            try:
                counter = report.find("counter[@type='LINE']")
                covered, missed = count(counter, "covered"), count(counter, "missed")
                value = (
                    f"{100 * covered / (covered + missed):.2f}%"
                    if covered + missed else "N/A (no lines)"
                )
            except ValueError:
                problems.append(f"Malformed: {path}")
        rows.append(f"| {module} | {value} | 90% |")
    return rows + [""]


def checkstyle_summary(root, problems):
    counts = Counter()
    findings = []
    paths = [Path("build/reports/checkstyle/allJava.xml")] + [
        Path(module) / "build/reports/checkstyle/main.xml" for module in PUBLISHED_MODULES
    ]
    available = 0
    for path in paths:
        report = xml_report(root, path, "checkstyle", problems)
        if report is None:
            continue
        errors = [
            (source.get("name"), error)
            for source in report.findall("file") for error in source.findall("error")
        ]
        if any(
            not name or error.get("severity") not in ("error", "warning", "info", "ignore")
            or not error.get("line", "").isdigit() or "message" not in error.attrib
            for name, error in errors
        ):
            problems.append(f"Malformed: {path}")
            continue
        available += 1
        for name, error in errors:
            counts[error.get("severity")] += 1
            source = Path(name)
            if source.is_absolute():
                source = (
                    source.relative_to(root) if source.is_relative_to(root) else Path(source.name)
                )
            findings.append(f"{source}:{error.get('line')}: {error.get('message')}")
    result = (
        f"{counts['error']} error{'s' if counts['error'] != 1 else ''}; "
        f"{counts['warning']} warning{'s' if counts['warning'] != 1 else ''} "
        "(negative fixtures excluded)"
    )
    return availability(result, available, len(paths)), findings


def availability(text, available, expected):
    if not available:
        return "Unavailable (reports not produced or unreadable)"
    return text if available == expected else f"Partial: {text}"


def conformance_summary(root, problems):
    base = Path("conformance/build/reports/conformance")
    manifest = Path("conformance/src/test/resources/required-server-scenarios.txt")
    runs = [path for path in (root / base).glob("run-*") if path.is_dir()]
    if not runs:
        problems.append(f"Missing: {base}/run-*")
        return "Unavailable", "Unavailable", [], []
    try:
        required = set((root / manifest).read_text(encoding="utf-8").split())
        if not required:
            raise ValueError("Empty manifest")
    except (OSError, ValueError):
        problems.append(f"Missing or malformed: {manifest}")
        return "Unavailable", "Unavailable", [], []
    run = max(runs, key=lambda path: path.stat().st_mtime_ns)
    counts = {True: Counter(), False: Counter()}
    failures = {True: [], False: []}
    seen = set()
    statuses = ("SUCCESS", "FAILURE", "WARNING", "SKIPPED", "INFO")
    for path in sorted(run.glob("*/checks.json")):
        try:
            name = re.fullmatch(r"server-(.+)-\d{4}-\d{2}-\d{2}T.+", path.parent.name)
            checks = json.loads(path.read_text(encoding="utf-8"))
            if not name or name[1] in seen or not isinstance(checks, list) or not checks:
                raise ValueError("Invalid scenario report")
            if any(
                not isinstance(check, dict) or check.get("status") not in statuses
                for check in checks
            ):
                raise ValueError("Invalid check status")
            scenario = name[1]
            seen.add(scenario)
            scored = scenario in required
            counts[scored].update(check["status"] for check in checks)
            failures[scored].extend(
                f"{scenario}: {check.get('name', check.get('id', 'Unnamed check'))}"
                for check in checks if check["status"] == "FAILURE"
            )
        except (OSError, ValueError):
            problems.append(f"Malformed: {path.relative_to(root)}")
    missing = required - seen
    problems.extend(f"Missing: conformance scenario {name}" for name in sorted(missing))
    descriptions = {
        scored: ", ".join(
            f"{status}={counts[scored][status]}" for status in statuses if counts[scored][status]
        ) or "no checks available"
        for scored in (True, False)
    }
    return (
        f"{'Partial: ' if missing else ''}{len(required & seen)}/{len(required)} "
        f"required scenario reports; {descriptions[True]}",
        f"{len(seen - required)} non-scored scenario reports; {descriptions[False]}",
        failures[True], failures[False],
    )


def test_summary(root, problems):
    counts = Counter()
    conformance_counts = Counter()
    failures = []
    paths = sorted({
        path for pattern in (
            "*/build/test-results/*/TEST-*.xml",
            "examples/*/build/test-results/*/TEST-*.xml",
        ) for path in root.glob(pattern)
    })
    modules = {path.relative_to(root).as_posix().split("/build/")[0] for path in paths}
    missing = set(TEST_MODULES) - modules
    problems.extend(f"Missing: {module}/build/test-results" for module in sorted(missing))
    available = 0
    for path in paths:
        path = path.relative_to(root)
        suite = xml_report(root, path, "testsuite", problems)
        if suite is None:
            continue
        try:
            values = {
                key: count(suite, key) for key in ("tests", "failures", "errors", "skipped")
            }
            if values["tests"] < values["failures"] + values["errors"] + values["skipped"]:
                raise ValueError("Inconsistent test counts")
        except ValueError:
            problems.append(f"Malformed: {path}")
            continue
        available += 1
        counts.update(values)
        if path.parts[0] == "conformance":
            conformance_counts.update(values)
        for case in suite.findall("testcase"):
            for failure in (*case.findall("failure"), *case.findall("error")):
                failures.append(
                    f"{case.get('classname')}.{case.get('name')}: "
                    f"{failure.get('message', '')}"
                )
    passed = counts["tests"] - counts["failures"] - counts["errors"] - counts["skipped"]
    result = (
        f"{counts['tests']} total; {passed} passed; {counts['failures']} failed; "
        f"{counts['errors']} errored; {counts['skipped']} skipped"
    )
    conformance = "Unavailable"
    if conformance_counts["tests"]:
        if conformance_counts["failures"] or conformance_counts["errors"]:
            conformance = "Failed"
        elif conformance_counts["skipped"]:
            conformance = "Skipped"
        else:
            conformance = "Passed"
    return availability(result, available, len(paths) + len(missing)), failures, conformance


def render(root, env):
    problems = []
    tests, failures, conformance_verdict = test_summary(root, problems)
    checkstyle, findings = checkstyle_summary(root, problems)
    conformance, diagnostics, required_failures, diagnostic_failures = conformance_summary(
        root, problems
    )
    status = {
        "success": "Passed", "failure": "Failed", "skipped": "Skipped",
        "cancelled": "Cancelled",
    }.get(env.get("BUILD_OUTCOME"), "Unavailable")
    sonar = {
        "success": "Passed (analysis and quality gate)",
        "failure": "Failed (analysis or quality gate)", "skipped": "Skipped",
        "cancelled": "Cancelled",
    }.get(env.get("SONAR_OUTCOME"), "Unavailable")
    query = {"id": "SuppieRK_mcp-server-kit-java"}
    if env.get("QUALITY_PR_NUMBER"):
        query["pullRequest"] = env["QUALITY_PR_NUMBER"]
    else:
        query["branch"] = env.get("GITHUB_REF_NAME") or "main"
    dashboard = "https://sonarcloud.io/dashboard?" + urlencode(query)
    lines = [
        "## Quality report", "", "| Check | Result |", "| --- | --- |",
        f"| Build | {status} |", f"| Tests | {tests} |",
        f"| Checkstyle | {checkstyle} |",
        f"| Required conformance | {conformance_verdict}; {conformance} |",
        f"| Upstream diagnostics | {diagnostics} |",
        f"| Sonar | {sonar}; [dashboard]({dashboard}) |", "",
    ]
    lines.extend(coverage_rows(root, problems))
    for title, entries in (
        ("Test failures", failures), ("Checkstyle findings", findings),
        ("Required conformance failures", required_failures),
        ("Non-scored diagnostic failures (do not determine the gate)", diagnostic_failures),
        ("Unavailable reports", sorted(problems, key=lambda item: item.startswith("Missing:"))),
    ):
        if entries:
            lines.extend(["<details>", f"<summary>{title}</summary>", ""])
            lines.extend(f"- {safe_text(entry)}" for entry in entries[:10])
            if len(entries) > 10:
                lines.append(f"- {len(entries) - 10} more; see the report ZIP or build log.")
            lines.extend(["", "</details>", ""])
    artifact = env.get("QUALITY_ARTIFACT_URL", "")
    valid_artifact = re.fullmatch(
        r"https://github\.com/[^?#\s<>()\[\]]+/actions/runs/\d+/artifacts/\d+", artifact
    )
    lines.append(
        f"[Download detailed reports (ZIP)]({artifact})"
        if valid_artifact else "Report ZIP unavailable."
    )
    return "\n".join(lines)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd())
    args = parser.parse_args()
    print(render(args.root.resolve(), os.environ))
