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
python3 tools/record_abi.py 1.0 --publish-styling --label 1.0-probes
```

Then upgrade only Component:

```sh
git switch codex/contract-1.1
python3 tools/record_abi.py 1.1 --label 1.1-probes --baseline-label 1.0-probes
python3 tools/compare_abi.py
```

The recorder publishes Component, publishes Styling only during the 1.0 stage, verifies App,
checks the frozen Styling AAR checksum, and saves original evidence under `artifacts/1.0-probes` and
`artifacts/1.1-probes`: Maven publications and source JARs, extracted class JARs, complete javap signatures,
bytecode and verbose dumps, dependency reports, raw build logs, JUnit XML/HTML, lint reports,
debug APKs, build configuration, manifests, and SHA-256 checksums.

The App suite has exactly one test for each of the 12 current probes. With frozen Styling 1.0,
Component 1.1 fails `v1CopyDefault` and `v1CopyFull` because old inline copies lose the new border
callback, and `jvmOverloadsDefault` because the old `$default` method is absent. V2 and
`@IntroducedAt` calls pass. The recorder checks this exact result. Original 18-test recordings
under `artifacts/1.0` and `artifacts/1.1` remain unchanged; new recordings require a fresh label.

## Existing contract behavior tests

`Contract1Test` and `Contract2Test` still contain exactly `isDelegated` and `isRebinding`.
V1 delegates copied defaults to the original; V2 rebinds callbacks to the copied receiver.
Those separate four tests intentionally have two failures, as documented in their report.
`rememberCopy` retains the first copy at a composition location, matching Benchmark.

`app` displays two buttons using presets from the frozen Styling library. No compiler plugin
or benchmark module is used.

Current 12-test results: [PROBE_RECOMPILATION.md](docs/PROBE_RECOMPILATION.md).

## Recompile the current probes

The current `BinaryProbes.kt` is identical in both release branches. To compare the frozen and
rebuilt consumers on `codex/contract-1.1`:

```sh
./gradlew :app:testDebugUnitTest --tests '*BinaryCompatibilityTest'
# Expected: 9 pass, 3 fail with Styling 1.0.

./gradlew :styling:publishReleasePublicationToMavenLocal -PstylingVersion=1.1
./gradlew :app:testDebugUnitTest --tests '*BinaryCompatibilityTest' -PstylingVersion=1.1
# Expected: all 12 pass with Styling 1.1 compiled against Component 1.1.
```

`stylingVersion` selects both the published Styling version and the Component version used
to compile it. Omitting it keeps Styling pinned to 1.0. Rebuilding publishes a separate 1.1
artifact; it does not overwrite the frozen 1.0 artifact.
