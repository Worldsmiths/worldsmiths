package io.github.worldsmiths.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.bundling.Zip;

/**
 * Zips the {@code robovmInstall} tree for CI upload (Dp peer of distZip / itch zip / debug APK).
 * Usable from a fixture path via {@code :ios:wsRobovmInstallZip}.
 */
public abstract class WsRobovmInstallZip extends Zip {

    public static final String TASK_NAME = "wsRobovmInstallZip";

    public static final String ARCHIVE_FILE_NAME = "ios-robovm-install.zip";

    @InputDirectory
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getInstallDirectory();

    public WsRobovmInstallZip() {
        setGroup("worldsmiths");
        setDescription("Zip the robovmInstall tree for CI ship upload");
        getArchiveFileName().set(ARCHIVE_FILE_NAME);
        from(getInstallDirectory());
    }
}
