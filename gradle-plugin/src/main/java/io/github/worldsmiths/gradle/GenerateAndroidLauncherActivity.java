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
 * Writes the Android launcher Activity that adapts {@code AndroidApplicationHost} and calls
 * {@code AndroidBackend.attach}.
 */
@CacheableTask
public abstract class GenerateAndroidLauncherActivity extends DefaultTask {

    static final String MAIN_CLASS = "io.github.worldsmiths.generated.android.AndroidLauncher";

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
        String code = "package io.github.worldsmiths.generated.android;\n"
                + "\n"
                + "import android.os.Bundle;\n"
                + "import com.badlogic.gdx.ApplicationListener;\n"
                + "import com.badlogic.gdx.backends.android.AndroidApplication;\n"
                + "import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;\n"
                + "import io.github.worldsmiths.android.AndroidApplicationHost;\n"
                + "import io.github.worldsmiths.android.AndroidBackend;\n"
                + "import io.github.worldsmiths.android.AndroidConfig;\n"
                + "import io.github.worldsmiths.core.WorldsmithsGame;\n"
                + "\n"
                + "public class AndroidLauncher extends AndroidApplication implements AndroidApplicationHost {\n"
                + "    @Override\n"
                + "    protected void onCreate(Bundle savedInstanceState) {\n"
                + "        super.onCreate(savedInstanceState);\n"
                + "        WorldsmithsGame game = new " + gameClass + "();\n"
                + "        AndroidBackend.attach(this, game, new AndroidConfig("
                + width + ", " + height + "));\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void initialize(ApplicationListener listener, AndroidConfig config) {\n"
                + "        AndroidApplicationConfiguration gdxConfig = new AndroidApplicationConfiguration();\n"
                + "        gdxConfig.useImmersiveMode = config.isUseImmersiveMode();\n"
                + "        gdxConfig.useAccelerometer = config.isUseAccelerometer();\n"
                + "        gdxConfig.useGyroscope = config.isUseGyroscope();\n"
                + "        gdxConfig.useCompass = config.isUseCompass();\n"
                + "        gdxConfig.useWakelock = config.isUseWakelock();\n"
                + "        initialize(listener, gdxConfig);\n"
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
