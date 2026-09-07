# Protocol benchmark

This unpublished JMH 1.37 module measures the actual isolated core artifact through public kit APIs.
It covers `tools/list`, a small tool call, and a nested call with input/output schema validation.
Each workload measures decode, encode, and the complete decode/invoke/validate/encode pipeline.
Setup checks successful terminal responses so failures cannot masquerade as fast invocations.

Run the Gradle `:benchmarks:benchmark` task with the `benchmarkArgs` project property, for example:
`ProtocolBenchmark -f 2 -wi 3 -i 3 -w 1s -r 1s -prof gc -foe true -rf json -rff build/results.json`.
Results are written relative to this module. JMH is not a release gate, and it is not included in
published dependencies. Do not benchmark concurrently with builds, tests, or other heavy workloads.

Short local measurements are screening evidence, not a claim that any JSON implementation is
universally fastest. Compare end-to-end throughput and allocations, not only parser throughput.
