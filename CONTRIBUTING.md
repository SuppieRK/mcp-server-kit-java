# Contributing

Thank you for your contribution.

## Before you start

Use a GitHub issue for a large change. Describe the problem and the smallest useful solution. Do not include unrelated changes.

## Build the project

The Gradle daemon and toolchains use Java 25. Gradle downloads a matching JDK when necessary. The
published modules and all examples except Micronaut compile with `--release 17`. The Micronaut
example compiles with `--release 25`. Run this command before you open a pull request:

```text
./gradlew check
```

Run `./gradlew spotlessApply` to format Java and Gradle files. Add tests for changed behavior. Keep line coverage at or above 90% in each published module.

Public and protected production API methods must have appropriate Javadocs or a valid inherited
contract. Write technical text in ASD-STE100 Simplified Technical English. Keep sentences short.
Use one term for one concept. Use active voice where practical. State requirements unambiguously.

## Java quality checks

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
