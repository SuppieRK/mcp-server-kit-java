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

All methods in production Java source must have appropriate Javadocs. Write technical text in
ASD-STE100 Simplified Technical English. Keep sentences short. Use one term for one concept. Use
active voice where practical. State requirements unambiguously.

## Keep framework responsibilities in the host

The library supplies protocol, dispatch, transport, and adapter building blocks. It does not supply
an application server or a security framework. Framework examples own their listeners,
authentication, authorization, and route composition.

Test an example endpoint through the framework's idiomatic HTTP test client. Verify the HTTP status,
headers, and MCP response body that a user receives. Do not bypass the REST endpoint in an
integration test.

## Submit the change

Use a focused commit message. Explain the user-visible effect in the pull request. State which tests you ran. By submitting a contribution, you agree that the project can distribute it under the MIT License.
