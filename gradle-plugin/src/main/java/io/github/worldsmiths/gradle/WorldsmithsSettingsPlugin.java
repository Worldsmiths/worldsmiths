package io.github.worldsmiths.gradle;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

/**
 * Includes a generated {@code :desktop} subproject that depends on the user's game module, and
 * optionally {@code :web} when {@code targets.web {}} is configured.
 *
 * <p>Only tiny, deterministic files are written during settings evaluation, through
 * {@link GeneratedModuleSource} so they stay configuration-cache clean. Real generation (launcher
 * sources, registries) belongs in tasks of the generated project.
 */
public final class WorldsmithsSettingsPlugin implements Plugin<Settings> {

    static final String WORLDSMITHS_DIR = ".worldsmiths";
    static final String DESKTOP_DIR = "gen/desktop";
    static final String WEB_DIR = "gen/web";

    // Keep in sync with DesktopConfig / WebConfig defaults (plugin has no backends dep).
    static final String DEFAULT_TITLE = "Worldsmiths";
    static final int DEFAULT_WIDTH = 800;
    static final int DEFAULT_HEIGHT = 600;
    static final String DEFAULT_ASSETS_DIR = "assets";
    static final boolean DEFAULT_VSYNC = true; // DesktopConfig.DEFAULT_VSYNC
    static final boolean DEFAULT_RESIZABLE = true; // DesktopConfig.DEFAULT_RESIZABLE
    /** Same default as WebConfig.DEFAULT_STORAGE_PREFIX / gdx-teavm storagePrefix. */
    static final String DEFAULT_STORAGE_PREFIX = "app";

    @Override
    public void apply(Settings settings) {
        WorldsmithsExtension extension = settings.getExtensions().create("worldsmiths", WorldsmithsExtension.class);
        extension.getGameModule().convention(":core");
        extension.getAssetsDir().convention(DEFAULT_ASSETS_DIR);
        DesktopTargetExtension desktop = extension.getTargets().getDesktop();
        desktop.getTitle().convention(DEFAULT_TITLE);
        desktop.getWidth().convention(DEFAULT_WIDTH);
        desktop.getHeight().convention(DEFAULT_HEIGHT);
        desktop.getVsync().convention(DEFAULT_VSYNC);
        desktop.getResizable().convention(DEFAULT_RESIZABLE);
        WebTargetExtension web = extension.getTargets().getWeb();
        web.getTitle().convention(DEFAULT_TITLE);
        web.getWidth().convention(DEFAULT_WIDTH);
        web.getHeight().convention(DEFAULT_HEIGHT);
        web.getStoragePrefix().convention(DEFAULT_STORAGE_PREFIX);

        settings.getGradle().settingsEvaluated(s -> includeGeneratedTargets(s, extension));
    }

    private static void includeGeneratedTargets(Settings settings, WorldsmithsExtension extension) {
        String gameModule = extension.getGameModule().get();
        if (settings.findProject(gameModule) == null) {
            throw new GradleException("worldsmiths.gameModule '" + gameModule
                    + "' is not included. Add include(\"" + gameModule + "\") to settings.gradle.kts.");
        }
        if (!extension.getGameClass().isPresent()) {
            throw new GradleException("worldsmiths.gameClass is not set in settings.gradle.kts.");
        }

        DesktopTargetExtension desktop = extension.getTargets().getDesktop();
        String assetsDir = extension.getAssetsDir().get();
        File worldsmithsDir = new File(settings.getRootDir(), WORLDSMITHS_DIR);
        Map<String, String> files = new LinkedHashMap<>();
        files.put(".gitignore", "*\n");
        files.put(DESKTOP_DIR + "/build.gradle.kts", desktopBuildScript(
                gameModule,
                extension.getGameClass().get(),
                assetsDir,
                desktop.getTitle().get(),
                desktop.getWidth().get(),
                desktop.getHeight().get(),
                desktop.getVsync().get(),
                desktop.getResizable().get(),
                desktop.getIcon().getOrNull()));

        boolean webConfigured = extension.getTargets().isWebConfigured();
        if (webConfigured) {
            WebTargetExtension web = extension.getTargets().getWeb();
            files.put(WEB_DIR + "/build.gradle.kts", webBuildScript(
                    gameModule,
                    extension.getGameClass().get(),
                    assetsDir,
                    web.getTitle().get(),
                    web.getWidth().get(),
                    web.getHeight().get(),
                    web.getStoragePrefix().get()));
        }

        settings.getProviders().of(GeneratedModuleSource.class, spec -> {
            spec.getParameters().getDirectory().set(worldsmithsDir);
            spec.getParameters().getFiles().set(files);
        }).get();

        settings.include(":desktop");
        settings.project(":desktop").setProjectDir(new File(worldsmithsDir, DESKTOP_DIR));

        if (webConfigured) {
            settings.include(":web");
            settings.project(":web").setProjectDir(new File(worldsmithsDir, WEB_DIR));
        }
    }

    static String desktopBuildScript(
            String gameModule,
            String gameClass,
            String assetsDir,
            String title,
            int width,
            int height,
            boolean vsync,
            boolean resizable,
            String icon) {
        StringBuilder script = new StringBuilder();
        script.append("// Generated by io.github.worldsmiths.engine. Do not edit; changes are overwritten.\n")
                .append("plugins { id(\"io.github.worldsmiths.desktop\") }\n")
                .append("\n")
                .append("worldsmithsDesktop {\n")
                .append("    gameModule = \"").append(escape(gameModule)).append("\"\n")
                .append("    gameClass = \"").append(escape(gameClass)).append("\"\n")
                .append("    assetsDir = \"").append(escape(assetsDir)).append("\"\n")
                .append("    title = \"").append(escape(title)).append("\"\n")
                .append("    width = ").append(width).append("\n")
                .append("    height = ").append(height).append("\n")
                .append("    vsync = ").append(vsync).append("\n")
                .append("    resizable = ").append(resizable).append("\n");
        if (icon != null) {
            script.append("    icon = \"").append(escape(icon)).append("\"\n");
        }
        script.append("}\n");
        return script.toString();
    }

    static String webBuildScript(
            String gameModule,
            String gameClass,
            String assetsDir,
            String title,
            int width,
            int height,
            String storagePrefix) {
        return "// Generated by io.github.worldsmiths.engine. Do not edit; changes are overwritten.\n"
                + "plugins { id(\"io.github.worldsmiths.web\") }\n"
                + "\n"
                + "worldsmithsWeb {\n"
                + "    gameModule = \"" + escape(gameModule) + "\"\n"
                + "    gameClass = \"" + escape(gameClass) + "\"\n"
                + "    assetsDir = \"" + escape(assetsDir) + "\"\n"
                + "    title = \"" + escape(title) + "\"\n"
                + "    width = " + width + "\n"
                + "    height = " + height + "\n"
                + "    storagePrefix = \"" + escape(storagePrefix) + "\"\n"
                + "}\n";
    }

    private static String escape(String value) {
        return JavaStringEscapes.escape(value);
    }
}
