# Spike S3: libGDX on iOS with RoboVM

A standalone Gradle build, not included from the root build. It contains a libGDX `core` module that clears the screen and an `ios` module set up like the iOS module gdx-liftoff generates (`robovm.xml`, `robovm.properties`, `Info.plist.xml`, `IOSLauncher`). The result is recorded in [ADR-0004](../../docs/adr/0004-java-baseline.md).

CI: `.github/workflows/spike-ios.yml` runs on `macos-latest`. Each matrix row builds an unsigned arm64 device IPA, runs the Java-level probes in the iPhone simulator, and uploads the IPA and logs.

## Parameters

| Gradle property | Default | Meaning |
|---|---|---|
| `robovmVersion` | `2.3.27` | MobiVM RoboVM version (plugin and runtime) |
| `javaRelease` | `8` | `javac --release` for both modules |
| `probe` | `none` | Extra probe source set: `none`, `java8api`, `java11`, `java17`, `java17norecords` |
| `simulatorArgs` | `--s3-probes` | Launch arguments for `launchIPhoneSimulator`. `--s3-smoke` runs the libGDX app for 60 frames |

## Running on a Mac with Xcode

```bash
./gradlew :ios:createIPA                       # unsigned IPA in ios/build/robovm/
./gradlew :ios:launchIPhoneSimulator           # prints S3PROBE lines and exits
./gradlew :ios:launchIPhoneSimulator -PsimulatorArgs=--s3-smoke
```

Signing is skipped (`iosSkipSigning = true`), so the IPA does not install on a non-jailbroken device. A signed build needs `iosSignIdentity` and `iosProvisioningProfile`.
