# ADR-0004: Java baseline

- Status: accepted, confirmed by spike S3 on 2026-11-13
- Date: 2026-11-12
- Deciders: founder
- Related: HLD Q1, [R02 §1.2], spike S3 (`spikes/ios-robovm/`)

## Context

RoboVM 2.3, the stable libGDX iOS path, accepts Java 8 syntax and Java 7 APIs. RoboVM 3's newer runtime is experimental. Desktop, Android and TeaVM can all consume Java 8 bytecode. The build itself (Gradle, AGP, Construo) needs JDK 17 or newer, and JDK 21 is the practical default.

## Options considered

1. **Runtime at Java 8 syntax and Java 7 APIs. Tooling on Java 21.** Matches libGDX's own core and keeps the stable iOS backend available.
2. **Java 11 for the runtime**, depending on RoboVM 3.
3. **Java 17 for the runtime**, which drops stable iOS until RoboVM 3 or Multi-OS Engine is proven.

## Decision

Option 1. Modules under `runtime/` and the platform launchers compile with `--release 8` and stay within Java 7 APIs. The Gradle plugin, editor and CLI target Java 21. The build JDK is 21.

The iOS backend is libGDX's `gdx-backend-robovm-metalangle` on MobiVM RoboVM 2.3.x (2.3.27 today), as gdx-liftoff generates it. iOS stays in M2 (FR-B09).

Revisit when RoboVM 3 leaves experimental status and its runtime covers Java 11 APIs, or when libGDX moves its iOS backend to RoboVM 3.

## Evidence from spike S3

Hello world on libGDX 1.14.2, built by `.github/workflows/spike-ios.yml` on `macos-latest` (macOS 26.7 arm64, Xcode 26.6, Temurin 21.0.13, Gradle 9.9.1). Every row builds an unsigned arm64 device IPA with `createIPA` and `iosSkipSigning = true`, then runs probes in the iPhone simulator (iOS 26.5). No Apple signing account was used.

These were the newest versions on Maven Central: libGDX 1.14.2, and RoboVM 2.3.27 and 3.0.0. RoboVM 3.0.1 has a GitHub release but is not published to Maven Central, so the roadmap's "RoboVM 3.0.1" could not be tested. `gdx-backend-robovm-metalangle` 1.14.2 declares RoboVM 2.3.24, and Gradle resolves it to the RoboVM version the app declares.

| RoboVM | `--release` | Code | IPA | Simulator |
|---|---|---|---|---|
| 2.3.27 | 8 | Java 8 syntax, Java 7 APIs | yes | all probes pass |
| 2.3.27 | 8 | Java 8 APIs (`Optional`, streams, `java.util.function`, `List.sort`) | yes, with "phantom class" warnings | `NoClassDefFoundError` and `NoSuchMethodError` |
| 2.3.27 | 11 | Java 8 syntax, Java 7 APIs | yes | all probes pass |
| 2.3.27 | 17 | Switch expressions, text blocks, pattern `instanceof`, sealed types | yes | pass, except the API `Class.isSealed` |
| 2.3.27 | 17 | Records | **no**: the compiler fails with `Attempt to create RefType containing a /` | |
| 3.0.0 | 8 | Java 8 syntax with Java 8 APIs | yes | all probes pass (one of four runs printed nothing) |
| 3.0.0 | 11 | `var`, `List.of`, `List.copyOf`, Java 11 APIs | yes | Java 8 to 10 APIs pass. `String.isBlank` and `Optional.isEmpty` throw `NoSuchMethodError` |
| 3.0.0 | 17 | Switch expressions, text blocks, pattern `instanceof`, sealed types | yes | pass, except the API `Class.isSealed` (two of three runs printed nothing) |
| 3.0.0 | 17 | Records | **no**: same compiler error as 2.3.27 | |

The probes run before UIKit starts. They cover lambdas, string concatenation (an `invokedynamic` call to `StringConcatFactory` at `--release 9` and later), nestmate private access (11 and later), try-with-resources and switch on strings. A separate smoke run starts the real libGDX app (UIKit plus MetalANGLE) in the simulator. On both RoboVM lines it reaches `create()` and renders its first frame. In one run on 3.0.0 it also reached the 60-frame marker and reported OpenGL ES 3.0 through ANGLE.

On the CI runner, the app output that RoboVM's simulator launcher relays sometimes stops early or never arrives, and no crash report is written. Only RoboVM 3.0.0 rows ever printed nothing at all. The simulator runs are evidence, not a CI gate. The IPA builds were stable across six runs.

What this shows:

- **The class file version is not the limit.** RoboVM 2.3.27 compiles and runs `--release 11` and `--release 17` bytecode, including the `StringConcatFactory` call sites and nestmates, even though it warns that `StringConcatFactory` is a phantom class.
- **The library API is the limit.** RoboVM 2.3.27's runtime is Java 7: no `java.util.function`, `Optional`, streams, `java.time` or default methods on collections. RoboVM 3.0.0's runtime stops at Java 10.
- **Records do not compile** on either RoboVM line.
- **RoboVM does not fail the build on missing APIs.** It prints `Warning: <class> is a phantom class!`, links anyway, and the app throws at runtime. A missing method on a class that exists, such as `List.sort`, produces no warning at all.
- RoboVM 3.0.0 needs a different `robovm.xml`, because the Harmony security providers that liftoff's config force-links are gone. Its IPA is about 25 MB against 16 MB for 2.3.27.

Keeping `--release 8` still pays off: javac then rejects Java 9 and later APIs and syntax, including records. A higher `--release` would give newer syntax but put APIs on the compile classpath that crash on the device.

## Consequences

- CI compiles the runtime once per target and fails on library APIs newer than Java 7, for example `java.util.Optional`, streams, `java.util.function` or `List.of`. `--release 8` alone does not catch Java 8 APIs, so the build plugin adds an API signature check (an animal-sniffer-style check against the RoboVM 2.3 runtime). The iOS build also fails on RoboVM "phantom class" warnings.
- Kotlin game code is still allowed (ADR-0012); the engine's own sources stay Java.
- Lambdas and method references are fine. Functional interfaces must be the engine's own or libGDX's, not `java.util.function`.
- An unsigned IPA builds on a stock GitHub macOS runner without Apple credentials. Signing, TestFlight and running on a device are still untested and need an Apple developer account.
- On iOS 26.5, UIKit warns that the `UIScene` lifecycle "will soon be required". libGDX 1.14.3 moves to UIScene and iOS 13 as the minimum, so the iOS launcher template changes on that upgrade.
- Spike S3 gate: passed. M2 keeps iOS.
