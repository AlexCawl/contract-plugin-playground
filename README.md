# Contract Plugin Playground

Documentation and original evidence are kept on `main`. The Maven Local experiment
lives on `codex/contract-1.0` and `codex/contract-1.1`; the application below is the initial playground.

- [Contracts V1 and V2](docs/CONTRACTS_REPORT.md)
- [JVM default arguments and version overloads](docs/JVM_DEFAULT_ARGUMENTS.md)
- [Binary compatibility experiment](docs/BINARY_COMPATIBILITY.md)
- [Frozen and rebuilt Styling probes](docs/PROBE_RECOMPILATION.md)
- [Original Kotlin/JVM signatures](docs/API_SIGNATURES.md)

A minimal Android Compose app that uses contracts across three Android modules:

- `component`: `Contract1`, `Contract2`, and independent `ContractButton1`/`ContractButton2` composables.
- `styling`: depends on `component` through `api` and provides composable `Contract1Presets.default` and `Contract2Presets.default` getters.
- `app`: depends directly on both libraries and displays the two buttons in one Activity. Click handlers are empty.

Both contracts expose `cornerSize()`, `shape()`, and `borderStroke()`. The interface defaults
derive a rounded shape from `cornerSize()` and return no border. Both presets provide 12 dp
corners and a 1 dp border in the current Material3 theme's `outline` color.

## Contract implementations

`Contract1` follows Benchmark V1: composable receiver callbacks, an inline anonymous-object
factory, and reflective equality/hashing. Its `copy` creates a wrapper that delegates omitted
overrides to the source through `wrapped`. Its composable `rememberCopy` uses `remember` without
keys, matching Benchmark: it retains the first copy at that composition location even if the
source contract changes.

`Contract2` follows Benchmark V2 and is implemented manually. It stores ordinary receiver
callbacks, compares the callbacks in `equals`/`hashCode`, and exposes them through a fresh
`Contract2.Contract` snapshot. Its `copy` preserves omitted callbacks and invokes them with
the new instance as receiver. Equality never evaluates callback results.

The real Contract Plugin, annotations, and benchmark modules are not dependencies of this project.

## Build and run

Use JDK 21, Android SDK Platform 36, and Build Tools 36.0.0. Modules use JVM target 17 and minSdk 23;
the app targets SDK 36. Versions match Benchmark: Gradle 9.6.1, AGP 9.4.1, Kotlin/Compose compiler
2.4.20, Compose BOM 2026.02.01, and Activity Compose 1.12.4. Kotlin support is built into AGP;
each module applies the Compose compiler plugin.

Set the SDK location in an ignored `local.properties` file:

```properties
sdk.dir=/path/to/Android/sdk
```

Build and lint all modules, including the intentionally failing comparison tests:

```sh
./gradlew build :app:assembleDebug --continue --max-workers=1
```

This command exits with a test failure by design. To assemble and lint without running tests:

```sh
./gradlew assemble lint --continue --max-workers=1
```

Run only the contract tests:

```sh
./gradlew :component:testDebugUnitTest
```

Install on a connected device and launch:

```sh
./gradlew :app:installDebug
adb shell am start -n com.alexcawl.contract.playground.app/.MainActivity
```

The screen should display `Contract 1` and `Contract 2` with the same rounded corners, borders,
and spacing clear of the system bars.

Each contract test class has exactly two tests, `isDelegated` and `isRebinding`. Both copy a
contract from 10 dp corners to 20 dp corners and check the dependent default shape.
The assertions deliberately describe mutually exclusive behavior:

| Test | Contract1 | Contract2 |
| --- | --- | --- |
| `isDelegated` | Pass: shape uses the original 10 dp corners. | Fail: shape uses the copied 20 dp corners. |
| `isRebinding` | Fail: shape uses the original 10 dp corners. | Pass: shape uses the copied 20 dp corners. |

The four-test suite intentionally reports two failures. V1 callbacks execute in a headless
Compose composition; Android tracing calls are no-ops in these local unit tests.
