package io.github.worldsmiths.gradle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/** Writes the Android Manifest for the generated {@code :android} module. */
@CacheableTask
public abstract class GenerateAndroidManifest extends DefaultTask {

    static final String LAUNCHER_ACTIVITY = "io.github.worldsmiths.generated.android.AndroidLauncher";

    @Input
    public abstract Property<String> getTitle();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() {
        String title = JavaStringEscapes.escapeXml(getTitle().get());
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <application\n"
                + "        android:label=\"" + title + "\"\n"
                + "        android:theme=\"@android:style/Theme.NoTitleBar.Fullscreen\">\n"
                + "        <activity\n"
                + "            android:name=\"" + LAUNCHER_ACTIVITY + "\"\n"
                + "            android:exported=\"true\"\n"
                + "            android:configChanges=\"keyboard|keyboardHidden|navigation|orientation|screenSize|screenLayout\">\n"
                + "            <intent-filter>\n"
                + "                <action android:name=\"android.intent.action.MAIN\" />\n"
                + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
                + "            </intent-filter>\n"
                + "        </activity>\n"
                + "    </application>\n"
                + "</manifest>\n";
        Path out = getOutputFile().get().getAsFile().toPath();
        try {
            Files.createDirectories(out.getParent());
            Files.write(out, xml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
