# iOS Phase-0 fixture

Pre-Snake proof that RoboVM applies on a settings-generated `:ios` module and packages under `--configuration-cache` (must-not-fail; reuse not required).

`assets/ws-ship-marker.txt` is mapped into RoboVM resources via `worldsmiths.assetsDir`. After install, assert the marker under `build/robovm/` and zip with `:ios:wsRobovmInstallZip` (same helper later `snake-ios` will use).

```bash
./gradlew -p fixtures/ios-phase0 :ios:robovmInstall :ios:wsRobovmInstallZip --configuration-cache -Probovm.iosSkipSigning=true
```

Requires macOS + Xcode. CI job: `ios-phase0`.

MetalANGLE (`gdx-backend-robovm-metalangle`) and `gdx-platform:natives-ios` contribute frameworks/libs through RoboVM’s classpath merge of each jar’s `META-INF/robovm/ios/robovm.xml` — the generated app `robovm.xml` does not hard-code `MetalANGLEKit`.
