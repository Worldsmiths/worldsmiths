# iOS Phase-0 fixture

Pre-Snake proof that RoboVM applies on a settings-generated `:ios` module and packages under `--configuration-cache` (must-not-fail; reuse not required).

```bash
./gradlew -p fixtures/ios-phase0 :ios:robovmInstall --configuration-cache -Probovm.iosSkipSigning=true
```

Requires macOS + Xcode. CI job: `ios-phase0`.
