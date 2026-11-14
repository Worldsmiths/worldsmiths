package io.github.worldsmiths.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.bundling.Zip;

/**
 * Zips the TeaVM JS webapp so {@code index.html} sits at the archive root (itch.io layout).
 */
public abstract class WsItchWebZip extends Zip {

    public static final String TASK_NAME = "wsItchWebZip";

    @InputDirectory
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getWebappDirectory();

    public WsItchWebZip() {
        setGroup("worldsmiths");
        setDescription("Package the TeaVM JS webapp as an itch-style zip (index.html at zip root)");
        // Force the ship name; BasePlugin conventions otherwise use the project name ("web").
        getArchiveFileName().set("web-itch.zip");
        from(getWebappDirectory());
    }
}
