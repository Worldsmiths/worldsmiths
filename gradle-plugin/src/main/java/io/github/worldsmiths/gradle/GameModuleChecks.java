package io.github.worldsmiths.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Shared checks against the user's game module. The launcher calls the game class directly and
 * compiles at a fixed release, so a newer game would build on one platform and fail on others.
 */
final class GameModuleChecks {

    private GameModuleChecks() {}

    /**
     * Requires the game module to compile at Java 8 (or leave non-Java modules alone). A module
     * that sorts before the host may already be evaluated when the host's {@code afterEvaluate}
     * runs.
     */
    static void requireJava8WhenReady(Project host, String gameModulePath) {
        Project game = host.project(gameModulePath);
        if (game.getState().getExecuted()) {
            requireJava8(game);
        } else {
            game.afterEvaluate(GameModuleChecks::requireJava8);
        }
    }

    static void requireJava8(Project game) {
        if (!game.getPluginManager().hasPlugin("java")) {
            return;
        }
        game.getTasks().withType(JavaCompile.class).forEach(compile -> {
            Integer release = compile.getOptions().getRelease().getOrNull();
            int level = release != null ? release.intValue() : javaMajor(compile.getTargetCompatibility());
            if (level > 8) {
                throw new GradleException("worldsmiths.gameModule '" + game.getPath()
                        + "' compiles for Java " + level
                        + ". Worldsmiths requires Java 8 so the same game can run on every platform.");
            }
        });
    }

    private static int javaMajor(String version) {
        String major = version.startsWith("1.") ? version.substring(2) : version;
        int dot = major.indexOf('.');
        if (dot >= 0) {
            major = major.substring(0, dot);
        }
        return Integer.parseInt(major);
    }
}
