package io.github.worldsmiths.gradle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * Writes RoboVM {@code robovm.xml}, {@code robovm.properties}, and a minimal {@code Info.plist.xml}
 * under {@code build/} (not settings-time).
 */
@CacheableTask
public abstract class GenerateIosRobovmConfig extends DefaultTask {

    @Input
    public abstract Property<String> getMainClass();

    @Input
    public abstract Property<String> getTitle();

    @Input
    public abstract Property<String> getAssetsDirAbsolute();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() {
        Path out = getOutputDir().get().getAsFile().toPath();
        String mainClass = getMainClass().get();
        String title = getTitle().get();
        String assetsDir = getAssetsDirAbsolute().get();
        String executable = "IosDelegate";
        String appId = "io.github.worldsmiths.generated.ios";

        String properties = "app.version=0.0.1\n"
                + "app.id=" + appId + "\n"
                + "app.mainclass=" + mainClass + "\n"
                + "app.executable=" + executable + "\n"
                + "app.build=1\n"
                + "app.name=" + title + "\n";

        // Paths in robovm.xml are resolved relative to the project directory; Info.plist is
        // referenced by absolute path so the config can live under build/.
        Path infoPlist = out.resolve("Info.plist.xml");
        String infoPlistPath = infoPlist.toAbsolutePath().toString().replace('\\', '/');
        String assetsPath = assetsDir.replace('\\', '/');

        StringBuilder resources = new StringBuilder();
        if (!assetsDir.isEmpty()) {
            resources.append("  <resources>\n")
                    .append("    <resource>\n")
                    .append("      <directory>")
                    .append(xmlEscape(assetsPath))
                    .append("</directory>\n")
                    .append("      <includes>\n")
                    .append("        <include>**</include>\n")
                    .append("      </includes>\n")
                    .append("      <skipPngCrush>true</skipPngCrush>\n")
                    .append("    </resource>\n")
                    .append("  </resources>\n");
        } else {
            resources.append("  <resources/>\n");
        }

        String robovmXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<config>\n"
                + "  <executableName>${app.executable}</executableName>\n"
                + "  <mainClass>${app.mainclass}</mainClass>\n"
                + "  <os>ios</os>\n"
                + "  <arch>arm64</arch>\n"
                + "  <target>ios</target>\n"
                + "  <iosInfoPList>" + xmlEscape(infoPlistPath) + "</iosInfoPList>\n"
                + "  <treeShaker>conservative</treeShaker>\n"
                + resources
                + "  <forceLinkClasses>\n"
                + "    <pattern>com.badlogic.gdx.scenes.scene2d.ui.*</pattern>\n"
                + "    <pattern>com.badlogic.gdx.graphics.g3d.particles.**</pattern>\n"
                + "    <pattern>com.android.okhttp.HttpHandler</pattern>\n"
                + "    <pattern>com.android.okhttp.HttpsHandler</pattern>\n"
                + "    <pattern>com.android.org.conscrypt.**</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.jce.provider.BouncyCastleProvider</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.jcajce.provider.keystore.BC$Mappings</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.jcajce.provider.keystore.bc.BcKeyStoreSpi</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.jcajce.provider.keystore.bc.BcKeyStoreSpi$Std</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.jce.provider.PKIXCertPathValidatorSpi</pattern>\n"
                + "    <pattern>com.android.org.bouncycastle.crypto.digests.AndroidDigestFactoryOpenSSL</pattern>\n"
                + "  </forceLinkClasses>\n"
                + "  <libs>\n"
                + "    <lib>z</lib>\n"
                + "  </libs>\n"
                + "  <frameworks>\n"
                + "    <framework>UIKit</framework>\n"
                + "    <framework>QuartzCore</framework>\n"
                + "    <framework>CoreGraphics</framework>\n"
                + "    <framework>OpenAL</framework>\n"
                + "    <framework>AudioToolbox</framework>\n"
                + "    <framework>AVFoundation</framework>\n"
                + "    <framework>GameController</framework>\n"
                + "    <framework>Metal</framework>\n"
                + "  </frameworks>\n"
                + "</config>\n";

        // Minimal plist: no launch storyboard / icon set (T4 can polish). Enough for robovmInstall.
        String infoPlistXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" "
                + "\"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
                + "<plist version=\"1.0\">\n"
                + "<dict>\n"
                + "  <key>CFBundleDevelopmentRegion</key>\n"
                + "  <string>en</string>\n"
                + "  <key>CFBundleDisplayName</key>\n"
                + "  <string>${app.name}</string>\n"
                + "  <key>CFBundleExecutable</key>\n"
                + "  <string>${app.executable}</string>\n"
                + "  <key>CFBundleIdentifier</key>\n"
                + "  <string>${app.id}</string>\n"
                + "  <key>CFBundleInfoDictionaryVersion</key>\n"
                + "  <string>6.0</string>\n"
                + "  <key>CFBundleName</key>\n"
                + "  <string>${app.name}</string>\n"
                + "  <key>CFBundlePackageType</key>\n"
                + "  <string>APPL</string>\n"
                + "  <key>CFBundleShortVersionString</key>\n"
                + "  <string>${app.version}</string>\n"
                + "  <key>CFBundleVersion</key>\n"
                + "  <string>${app.build}</string>\n"
                + "  <key>LSRequiresIPhoneOS</key>\n"
                + "  <true/>\n"
                + "  <key>UIStatusBarHidden</key>\n"
                + "  <true/>\n"
                + "  <key>MinimumOSVersion</key>\n"
                + "  <string>12.0</string>\n"
                + "  <key>UIDeviceFamily</key>\n"
                + "  <array>\n"
                + "    <integer>1</integer>\n"
                + "    <integer>2</integer>\n"
                + "  </array>\n"
                + "  <key>UIRequiredDeviceCapabilities</key>\n"
                + "  <array>\n"
                + "    <string>arm64</string>\n"
                + "  </array>\n"
                + "  <key>UISupportedInterfaceOrientations</key>\n"
                + "  <array>\n"
                + "    <string>UIInterfaceOrientationPortrait</string>\n"
                + "    <string>UIInterfaceOrientationPortraitUpsideDown</string>\n"
                + "    <string>UIInterfaceOrientationLandscapeLeft</string>\n"
                + "    <string>UIInterfaceOrientationLandscapeRight</string>\n"
                + "  </array>\n"
                + "  <key>UIRequiresFullScreen</key>\n"
                + "  <true/>\n"
                + "</dict>\n"
                + "</plist>\n";

        try {
            Files.createDirectories(out);
            Files.write(out.resolve("robovm.properties"), properties.getBytes(StandardCharsets.UTF_8));
            Files.write(out.resolve("robovm.xml"), robovmXml.getBytes(StandardCharsets.UTF_8));
            Files.write(infoPlist, infoPlistXml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String xmlEscape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
