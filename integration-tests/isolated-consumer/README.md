# Packaged consumer verification

Root `check` runs this independent build after staging the actual generated core JAR, POM,
Gradle module metadata, sources, and Javadocs in `build/verification-repository`.
It does not sign or publish anything externally.

Both Gradle-metadata and Maven-POM-only resolution must:

- Compile the consumer against only the core JAR and JDK (`--release 17`).
- Resolve no external core dependencies or dependency constraints.
- Use the exact staged JAR, not a stale cached snapshot.
- Run protocol encoding, decoding, tool invocation, schema validation, and fragment references.
- Repeat with host Jackson 2.20.0, Jackson 3.0.0, and NetworkNT 2.0.1 on the same runtime graph.
- Keep host versions unchanged and ignore the host's deliberately conflicting schema messages.

The consumer also checks public signatures/annotations, relocated class entries and resources.
These historical host versions are deliberate compatibility fixtures, not recommendations.
Native-image configuration is not verified or promised by this harness.
