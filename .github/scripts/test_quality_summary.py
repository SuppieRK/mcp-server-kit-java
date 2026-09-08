"""Exercise the CI reporting command with real report files."""

import os
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from xml.sax.saxutils import quoteattr


SCRIPT = Path(__file__).with_name("quality_summary.py")


class QualitySummaryTest(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)

    def report(self, path, content):
        target = self.root / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")

    def summary(self, **outcomes):
        env = {key: value for key, value in os.environ.items()
               if not key.startswith(("GITHUB_", "BUILD_", "SONAR_", "QUALITY_"))}
        env.update(outcomes)
        result = subprocess.run(
            [sys.executable, str(SCRIPT), "--root", str(self.root)],
            env=env, capture_output=True, text=True, check=False,
        )
        self.assertEqual(0, result.returncode, result.stderr)
        return result.stdout

    def test_summarizes_tests_from_modules_and_framework_examples(self):
        self.report("mcp-core/build/test-results/test/TEST-Core.xml", """
            <testsuite tests="4" failures="1" errors="0" skipped="1">
              <testcase classname="Core" name="broken">
                <failure message="Expected a value">PRIVATE STACK TRACE</failure>
              </testcase>
              <system-out>PRIVATE CAPTURED OUTPUT</system-out>
            </testsuite>""")
        self.report("examples/spring-webflux/build/test-results/test/TEST-Endpoint.xml", """
            <testsuite tests="3" failures="0" errors="1" skipped="0">
              <testcase classname="Endpoint" name="throws">
                <error message="Request failed">PRIVATE STACK TRACE</error>
              </testcase>
            </testsuite>""")
        output = self.summary(BUILD_OUTCOME="failure")
        self.assertIn("| Build | Failed |", output)
        self.assertIn("7 total; 4 passed; 1 failed; 1 errored; 1 skipped", output)
        self.assertIn("Core.broken", output)
        self.assertIn("Endpoint.throws", output)
        self.assertNotIn("PRIVATE", output)

    def test_shows_module_line_coverage_without_counting_nested_counters_twice(self):
        self.report("mcp-core/build/reports/jacoco/test/jacocoTestReport.xml", """
            <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
            <report name="mcp-core">
              <package name="nested"><counter type="LINE" covered="999" missed="0"/></package>
              <counter type="INSTRUCTION" covered="999" missed="0"/>
              <counter type="LINE" covered="3" missed="1"/>
            </report>""")
        output = self.summary()
        self.assertIn("| mcp-core | 75.00% | 90% |", output)
        self.assertIn("| mcp-transport-http | Unavailable | 90% |", output)

    def test_counts_code_findings_but_not_deliberate_checkstyle_fixtures(self):
        self.report("build/reports/checkstyle/allJava.xml", """
            <checkstyle><file name="mcp-core/src/main/java/Example.java">
              <error severity="error" line="7" message="Use an import"/>
            </file></checkstyle>""")
        self.report("mcp-core/build/reports/checkstyle/main.xml", """
            <checkstyle><file name="mcp-core/src/main/java/Example.java">
              <error severity="warning" line="9" message="Document this API"/>
            </file></checkstyle>""")
        self.report("build/reports/checkstyle/verifyCheckstyleRules.xml", """
            <checkstyle><file name="NegativeFixture.java">
              <error severity="error" line="1" message="EXPECTED FIXTURE FINDING"/>
            </file></checkstyle>""")
        output = self.summary()
        self.assertIn("1 error; 1 warning", output)
        self.assertIn("Example.java:7: Use an import", output)
        self.assertIn("Example.java:9: Document this API", output)
        self.assertIn("negative fixtures excluded", output)
        self.assertNotIn("EXPECTED FIXTURE FINDING", output)

    def test_labels_absent_malformed_and_partial_reports_without_claiming_success(self):
        empty = self.summary(BUILD_OUTCOME="failure")
        self.assertIn("| Tests | Unavailable", empty)
        self.assertIn("| Checkstyle | Unavailable", empty)
        self.assertNotIn("0 passed", empty)
        self.report("mcp-core/build/test-results/test/TEST-Core.xml",
                    '<testsuite tests="2" failures="0" errors="0" skipped="0"/>')
        self.report("examples/spring-webmvc/build/test-results/test/TEST-Endpoint.xml",
                    '<testsuite tests="broken"/>')
        self.report("build/reports/checkstyle/allJava.xml", "<checkstyle>")
        self.report("mcp-core/build/reports/jacoco/test/jacocoTestReport.xml", "<report/>")
        self.report("mcp-transport-http/build/reports/jacoco/test/jacocoTestReport.xml",
                    '<report><counter type="LINE" covered="0" missed="0"/></report>')
        output = self.summary(BUILD_OUTCOME="failure")
        self.assertIn("| Tests | Partial: 2 total; 2 passed", output)
        self.assertIn("Malformed:", output)
        self.assertIn("TEST-Endpoint.xml", output)
        self.assertIn("allJava.xml", output)
        self.assertIn("| mcp-core | Unavailable |", output)
        self.assertIn("| mcp-transport-http | N/A (no lines) |", output)
        self.assertIn("Missing:", output)

    def test_separates_required_conformance_checks_from_non_scored_diagnostics(self):
        self.report("conformance/src/test/resources/required-server-scenarios.txt", "tools-list\n")
        self.report("conformance/build/reports/conformance/run-1/summary.txt",
                    "MCP 2026-07-28 / conformance 0.2.0-alpha.11\n")
        self.report("conformance/build/reports/conformance/run-1/server-tools-list-2026-09-08T01-00-00Z/checks.json",
                    json.dumps([{"status": "SUCCESS"}, {"status": "WARNING"}, {"status": "SKIPPED"}]))
        self.report("conformance/build/reports/conformance/run-1/server-tasks-lifecycle-2026-09-08T01-00-00Z/checks.json",
                    json.dumps([{"status": "FAILURE", "name": "Task lifecycle", "details": "PRIVATE PAYLOAD"}]))
        output = self.summary()
        self.assertIn("1/1 required scenario reports", output)
        self.assertIn("SUCCESS=1, WARNING=1, SKIPPED=1", output)
        self.assertIn("non-scored", output)
        self.assertIn("FAILURE=1", output)
        self.assertIn("tasks-lifecycle: Task lifecycle", output)
        self.assertNotIn("PRIVATE PAYLOAD", output)

    def test_reports_actual_sonar_step_outcome_and_links_to_existing_artifact(self):
        artifact = "https://github.com/SuppieRK/mcp-server-kit-java/actions/runs/123/artifacts/456"
        for outcome, label in (
            ("success", "Passed (analysis and quality gate)"),
            ("failure", "Failed (analysis or quality gate)"),
            ("skipped", "Skipped"), ("cancelled", "Cancelled"), ("", "Unavailable"),
        ):
            with self.subTest(outcome=outcome):
                output = self.summary(SONAR_OUTCOME=outcome, QUALITY_ARTIFACT_URL=artifact,
                                      QUALITY_PR_NUMBER="42", SONAR_TOKEN="PRIVATE TOKEN")
                self.assertIn(f"| Sonar | {label}", output)
                self.assertIn("[Download detailed reports (ZIP)](" + artifact + ")", output)
                self.assertIn("pullRequest=42", output)
                self.assertNotIn("PRIVATE TOKEN", output)
        self.assertIn("Report ZIP unavailable", self.summary())

    def test_escapes_and_bounds_failure_details_and_rejects_unsafe_artifact_links(self):
        message = '<script>alert(1)</script> | [click](https://evil.invalid)\nPRIVATE SECOND LINE'
        cases = "".join(
            f'<testcase classname="Example" name="failure-{index:02}">'
            f'<failure message={quoteattr(message)}/></testcase>' for index in range(12)
        )
        self.report("mcp-core/build/test-results/test/TEST-Core.xml",
                    '<testsuite tests="12" failures="12" errors="0" skipped="0">' + cases + '</testsuite>')
        output = self.summary(QUALITY_ARTIFACT_URL="javascript:alert(1)")
        self.assertIn("12 failed", output)
        self.assertIn("&lt;script&gt;", output)
        self.assertIn("failure-09", output)
        self.assertNotIn("failure-10", output)
        self.assertIn("2 more; see the report ZIP", output)
        self.assertNotIn("<script>", output)
        self.assertNotIn("[click]", output)
        self.assertNotIn("PRIVATE SECOND LINE", output)
        self.assertNotIn("javascript:", output)
        self.assertLess(len(output.encode("utf-8")), 50_000)

    def test_uses_conformance_test_verdict_and_does_not_reuse_an_older_successful_run(self):
        self.report("conformance/src/test/resources/required-server-scenarios.txt", "tools-list\n")
        self.report("conformance/build/test-results/test/TEST-Conformance.xml", """
            <testsuite tests="1" failures="1" errors="0" skipped="0">
              <testcase classname="Conformance" name="requirements"><failure message="Runner timed out"/></testcase>
            </testsuite>""")
        self.report("conformance/build/reports/conformance/run-old/server-tools-list-2026-09-08T01-00-00Z/checks.json",
                    '[{"status":"SUCCESS"}]')
        self.report("conformance/build/reports/conformance/run-new/server-tools-list-2026-09-08T02-00-00Z/checks.json",
                    '{"not":"a check array"}')
        old = self.root / "conformance/build/reports/conformance/run-old"
        new = self.root / "conformance/build/reports/conformance/run-new"
        os.utime(old, ns=(1_000_000_000, 1_000_000_000))
        os.utime(new, ns=(2_000_000_000, 2_000_000_000))
        output = self.summary(BUILD_OUTCOME="failure")
        self.assertIn("| Required conformance | Failed; Partial: 0/1", output)
        self.assertIn("Malformed: conformance/build/reports/conformance/run-new", output)
        self.assertNotIn("SUCCESS=1", output)

    def test_does_not_treat_structurally_invalid_checkstyle_as_zero_findings(self):
        self.report("build/reports/checkstyle/allJava.xml",
                    '<checkstyle><file name="Example.java"><error/></file></checkstyle>')
        output = self.summary()
        self.assertIn("| Checkstyle | Unavailable", output)
        self.assertIn("Malformed: build/reports/checkstyle/allJava.xml", output)

    def test_renders_complete_passing_evidence_without_partial_or_unavailable_labels(self):
        modules = ("mcp-core", "mcp-transport-http", "mcp-transport-stdio",
                   "mcp-spring-webmvc", "mcp-spring-webflux", "mcp-javalin")
        examples = ("spring-webmvc", "spring-webflux", "javalin", "quarkus", "micronaut")
        passing = '<testsuite tests="2" failures="0" errors="0" skipped="0"/>'
        for module in modules + ("conformance",) + tuple(f"examples/{name}" for name in examples):
            self.report(f"{module}/build/test-results/test/TEST-Passing.xml", passing)
        self.report("examples/spring-webflux/build/test-results/integrationTest/TEST-Extra.xml", passing)
        self.report("build/reports/checkstyle/allJava.xml", "<checkstyle/>")
        for module in modules:
            self.report(f"{module}/build/reports/checkstyle/main.xml", "<checkstyle/>")
            self.report(f"{module}/build/reports/jacoco/test/jacocoTestReport.xml",
                        '<report><counter type="LINE" covered="9" missed="1"/></report>')
        self.report("conformance/src/test/resources/required-server-scenarios.txt", "tools-list\n")
        self.report("conformance/build/reports/conformance/run-1/server-tools-list-2026-09-08T01-00-00Z/checks.json",
                    '[{"status":"SUCCESS"}]')
        output = self.summary(
            BUILD_OUTCOME="success", SONAR_OUTCOME="success",
            QUALITY_ARTIFACT_URL="https://github.com/SuppieRK/mcp-server-kit-java/actions/runs/123/artifacts/456",
        )
        self.assertIn("| Tests | 26 total; 26 passed; 0 failed; 0 errored; 0 skipped |", output)
        self.assertIn("| Checkstyle | 0 errors; 0 warnings", output)
        self.assertIn("| Required conformance | Passed; 1/1", output)
        self.assertEqual(6, output.count("| 90.00% | 90% |"))
        self.assertNotIn("Partial", output)
        self.assertNotIn("Unavailable", output)


if __name__ == "__main__":
    unittest.main()
