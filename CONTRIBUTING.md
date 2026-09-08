# Contributing

Thank you for your contribution.

## Before you start

Use a GitHub issue for a large change. Describe the problem and the smallest useful solution. Do not include unrelated changes.

## Build the project

The Gradle daemon and toolchains use Java 25. Gradle downloads a matching JDK when necessary. The
published modules and all examples except Micronaut compile with `--release 17`. The Micronaut
example compiles with `--release 25`. Install Node.js 22 with npm for the upstream MCP conformance
test. Both executables must be on your `PATH`. Run this command before you open a pull request:

```text
./gradlew check
```

Run `./gradlew spotlessApply` to format Java and Gradle files. Add tests for changed behavior. Keep line coverage at or above 90% in each published module.

Public and protected production API methods must have appropriate Javadocs or a valid inherited
contract. Write technical text in ASD-STE100 Simplified Technical English. Keep sentences short.
Use one term for one concept. Use active voice where practical. State requirements unambiguously.

## MCP conformance test

Normal `./gradlew check` and `./gradlew build` include `:conformance:test`. CI runs the same test.
Run `./gradlew :conformance:test --rerun-tasks` to run it separately, including when it is up to date.
Gradle uses `npm ci` to install the locked test-only runner under `conformance/build/runner/`.
The first run requires access to the npm registry. No Node dependency enters a published module.

The test runs the official [MCP conformance runner](https://github.com/modelcontextprotocol/conformance)
at the explicitly pinned prerelease `0.2.0-alpha.11`. The stable `0.1.16` runner does not support
this kit's protocol revision. The test runs the full server SDK profile with `--requirements 2026-07-28`.
A test-only JDK HTTP host binds to an ephemeral loopback port. It forwards requests and responses
through `McpServerKit` and `StreamableHttpMcpTransport`.
The fixture supplies the diagnostic tools, resources, prompts, and application-owned workflows
that the upstream scenarios require. Client SDK and authorization-server tests are separate roles.

The [pinned profile](https://github.com/modelcontextprotocol/conformance/blob/c321dd32035556e6769d3724a8ee97d87c3faaac/requirements/2026-07-28.yaml)
runs 37 required server scenarios and 13 non-scored diagnostics. The test checks that all reports
exist and every required scenario actually exercised behavior. Any required failure fails the build.
Upstream warnings and individual capability-conditional skips remain visible. There is no local
expected-failure baseline. The optional Tasks extension is not implemented; its diagnostic failures
remain in the reports and do not affect the upstream profile's verdict. One Tasks diagnostic is
unconditionally skipped by this runner version. JSON Schema and header diagnostics also run.

The runner has a five-minute limit. Runner failure fails the build. Logs and upstream JSON reports
are under `conformance/build/reports/conformance/run-*/`. JUnit reports are under
`conformance/build/reports/tests/test/`. CI retains these reports even when the build fails.
When updating the runner, update both `conformance/package.json` and its lockfile. Review the pinned
requirements and `conformance/src/test/resources/required-server-scenarios.txt` before accepting
changes to the expected coverage. CI uses Node.js 22; the pinned npm dependency graph is test-only.

## Java quality checks

### Read CI results

Open **Actions**, select the **CI** run, and read **Quality report** on its summary page.
The report shows the build outcome, test totals, module line coverage, Checkstyle findings,
required conformance results, non-scored diagnostics, and the Sonar analysis/gate outcome.
Expand a failure section for up to ten entries. Use **Download detailed reports (ZIP)** for the
full reports and logs. GitHub requires sign-in to download artifacts; links expire with them.

The summary also runs after a failed build. Missing or malformed reports are marked unavailable
or partial, not successful. Expected Checkstyle fixture violations are excluded from code findings.
Conformance diagnostic failures are shown separately because they do not determine the gate.
Sonar can be skipped, for example when its token is unavailable on a fork pull request.

The CI-only summary script uses Python 3 and its standard library. CI runs its fixture tests:

```text
python3 -B -m unittest discover -s .github/scripts -p 'test_*.py' -v
```

Run `python3 -B .github/scripts/quality_summary.py` to preview available local reports as Markdown.
Local build and Sonar outcomes are unavailable unless supplied by CI. The summary reads existing
reports; it does not run checks or replace any quality gate.

### Run checks locally

Run `./gradlew checkstyleAllJava` for the shared Checkstyle hygiene and correctness rules. This checks
handwritten production code, tests, framework examples, benchmarks, isolated consumers, and shared
architecture tests. Generated build output and the deliberate Checkstyle regression fixtures are
excluded. Existing module `checkstyleMain` tasks separately enforce production API documentation.
Both policies are part of the normal `check` and `build` gate; warnings and errors fail the build.

Prefer explicit imports, including static imports. Do not use wildcard imports or unnecessary
package-qualified type names. Checkstyle's qualified-type rule does not cover every context: qualified
annotations, nested types, static-call qualifiers, class literals, references, and `permits` names
still need review. Required type-name disambiguation is allowed. Spotless owns formatting; ArchUnit
owns the fluent-builder and architecture contracts.

The profile also checks equality overrides, string-literal identity comparisons, stray semicolons,
switch fallthrough, empty catches, and double-brace initialization. Explain an intentionally empty
catch with a comment; naming its parameter `ignored` is not sufficient. For records, custom equality
overrides must define both `equals` and `hashCode`.

Run `./gradlew verifyCheckstyleRules` after changing the policy. It runs the real Checkstyle engine
against positive and negative fixtures and compares the exact reported rule and line with each
fixture's `expect` or `expect-next` marker. Expected fixture violations do not fail that verification;
missing or extra violations do. Its HTML report therefore deliberately contains violations.
Repository reports are under `build/reports/checkstyle/`; module documentation reports are under
each module's corresponding directory. CI retains them even when verification fails. Do not add
blanket source-set exclusions to hide failures.

## Keep framework responsibilities in the host

The library supplies protocol, dispatch, transport, and adapter building blocks. It does not supply
an application server or a security framework. Framework examples own their listeners,
authentication, authorization, and route composition.

Test an example endpoint through the framework's idiomatic HTTP test client. Verify the HTTP status,
headers, and MCP response body that a user receives. Do not bypass the REST endpoint in an
integration test.

## Submit the change

Use a focused commit message. Explain the user-visible effect in the pull request. State which tests you ran. By submitting a contribution, you agree that the project can distribute it under the MIT License.
