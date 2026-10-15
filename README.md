# Contract Plugin Playground

An Android binary-compatibility sandbox with three modules: `component`, `styling`, and `app`.
Both library dependencies in `app`, and the Component dependency in `styling`, resolve from
**Maven Local**, never from `project(...)` dependencies.

Detailed report: [BINARY_COMPATIBILITY.md](docs/BINARY_COMPATIBILITY.md).
Full Kotlin/JVM signatures: [API_SIGNATURES.md](docs/API_SIGNATURES.md).

## Two release branches

- `codex/contract-1.0`: Component 1.0 has `cornerSize()` and default `shape()`.
- `codex/contract-1.1`: Component 1.1 adds default `borderStroke()` and preserves old ABI entry points.
- Styling stays at **1.0**, compiled once against Component **1.0**. Its source and published AAR
  must not change during the upgrade. App's direct Component dependency selects the newer version.

Coordinates: `com.alexcawl.contract.playground:component:1.0` / `1.1` and
`com.alexcawl.contract.playground:styling:1.0`.

The two additional functions, `withJvmOverloads` and `withIntroducedAt`, have a required parameter
and an optional parameter in 1.0. In 1.1 each gets another optional parameter. Frozen Styling
contains calls with all old arguments and calls that omit the old optional argument.

## Record the experiment

Use the existing Android SDK configuration and Gradle wrapper. Compile target is JVM 17;
`component` explicitly uses `JvmDefaultMode.ENABLE`. The checked-in daemon criteria select
JDK 25. No Android device is required, and the recorder never invokes adb.

Start from the 1.0 branch:

```sh
git switch codex/contract-1.0
python3 tools/record_abi.py 1.0 --publish-styling
```

Then upgrade only Component:

```sh
git switch codex/contract-1.1
python3 tools/record_abi.py 1.1
python3 tools/compare_abi.py
```

The recorder publishes Component, publishes Styling only during the 1.0 stage, verifies App,
checks the frozen Styling AAR checksum, and saves original evidence under `artifacts/1.0` and
`artifacts/1.1`: Maven publications and source JARs, extracted class JARs, complete javap signatures,
bytecode and verbose dumps, dependency reports, raw build logs, JUnit XML/HTML, lint reports,
debug APKs, build configuration, manifests, and SHA-256 checksums.

The 1.1 App comparison intentionally fails only `jvmOverloadsDefault`: the old `$default` method
is absent. Both contracts and the `@IntroducedAt` function must remain binary compatible.
The recorder accepts exactly this outcome and rejects unexpected failures.

## Existing contract behavior tests

`Contract1Test` and `Contract2Test` still contain exactly `isDelegated` and `isRebinding`.
V1 delegates copied defaults to the original; V2 rebinds callbacks to the copied receiver.
Those separate four tests intentionally have two failures, as documented in their report.
`rememberCopy` retains the first copy at a composition location, matching Benchmark.

`app` displays two buttons using presets from the frozen Styling library. No compiler plugin
or benchmark module is used.
