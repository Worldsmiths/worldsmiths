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
 * Writes the iOS {@code IOSApplication.Delegate} that adapts {@code IosApplicationHost} and calls
 * {@code IosBackend.attach}.
 */
@CacheableTask
public abstract class GenerateIosDelegate extends DefaultTask {

    static final String MAIN_CLASS = "io.github.worldsmiths.generated.ios.IosDelegate";

    @Input
    public abstract Property<String> getGameClass();

    @Input
    public abstract Property<Integer> getWidth();

    @Input
    public abstract Property<Integer> getHeight();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() {
        Path source = getOutputDir().get().getAsFile().toPath()
                .resolve(MAIN_CLASS.replace('.', '/') + ".java");
        String gameClass = getGameClass().get();
        int width = getWidth().get();
        int height = getHeight().get();
        String code = "package io.github.worldsmiths.generated.ios;\n"
                + "\n"
                + "import com.badlogic.gdx.ApplicationListener;\n"
                + "import com.badlogic.gdx.backends.iosrobovm.IOSApplication;\n"
                + "import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;\n"
                + "import io.github.worldsmiths.core.WorldsmithsGame;\n"
                + "import io.github.worldsmiths.ios.IosApplicationHost;\n"
                + "import io.github.worldsmiths.ios.IosBackend;\n"
                + "import io.github.worldsmiths.ios.IosConfig;\n"
                + "import org.robovm.apple.foundation.NSAutoreleasePool;\n"
                + "import org.robovm.apple.uikit.UIApplication;\n"
                + "\n"
                + "public class IosDelegate extends IOSApplication.Delegate implements IosApplicationHost {\n"
                + "\n"
                + "    private IOSApplication application;\n"
                + "\n"
                + "    @Override\n"
                + "    protected IOSApplication createApplication() {\n"
                + "        WorldsmithsGame game = new " + gameClass + "();\n"
                + "        IosBackend.attach(this, game, new IosConfig("
                + width + ", " + height + "));\n"
                + "        return application;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void initialize(ApplicationListener listener, IosConfig config) {\n"
                + "        IOSApplicationConfiguration gdxConfig = new IOSApplicationConfiguration();\n"
                + "        gdxConfig.preventScreenDimming = config.isPreventScreenDimming();\n"
                + "        gdxConfig.orientationPortrait = config.isOrientationPortrait();\n"
                + "        gdxConfig.orientationLandscape = config.isOrientationLandscape();\n"
                + "        gdxConfig.useAccelerometer = config.isUseAccelerometer();\n"
                + "        gdxConfig.useCompass = config.isUseCompass();\n"
                + "        gdxConfig.hideHomeIndicator = config.isHideHomeIndicator();\n"
                + "        application = new IOSApplication(listener, gdxConfig);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(String[] argv) {\n"
                + "        NSAutoreleasePool pool = new NSAutoreleasePool();\n"
                + "        UIApplication.main(argv, null, IosDelegate.class);\n"
                + "        pool.close();\n"
                + "    }\n"
                + "}\n";
        try {
            Files.createDirectories(source.getParent());
            Files.write(source, code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
