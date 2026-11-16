package io.github.worldsmiths.gradle;

import java.lang.reflect.Method;
import java.net.URI;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

/**
 * Detects whether Google Maven ({@code google()}) is available for dependency resolution.
 * Checks project repositories and, when present, settings {@code dependencyResolutionManagement}.
 */
final class GoogleMavenRepositories {

    private GoogleMavenRepositories() {}

    static boolean isPresent(Project project) {
        if (containsGoogle(project.getRepositories())) {
            return true;
        }
        RepositoryHandler drmRepos = dependencyResolutionRepositories(project);
        return drmRepos != null && containsGoogle(drmRepos);
    }

    static boolean containsGoogle(RepositoryHandler repositories) {
        for (ArtifactRepository repository : repositories) {
            if (isGoogleRepository(repository)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGoogleRepository(ArtifactRepository repository) {
        String name = repository.getName();
        if (name != null && (name.equals("Google") || name.equalsIgnoreCase("google"))) {
            return true;
        }
        if (repository instanceof MavenArtifactRepository) {
            URI url = ((MavenArtifactRepository) repository).getUrl();
            if (url != null) {
                String text = url.toString();
                return text.contains("dl.google.com/dl/android/maven2")
                        || text.contains("maven.google.com");
            }
        }
        return false;
    }

    private static RepositoryHandler dependencyResolutionRepositories(Project project) {
        try {
            Object gradle = project.getGradle();
            Method getSettings = gradle.getClass().getMethod("getSettings");
            Object settings = getSettings.invoke(gradle);
            Method getDrm = settings.getClass().getMethod("getDependencyResolutionManagement");
            Object drm = getDrm.invoke(settings);
            Method getRepos = drm.getClass().getMethod("getRepositories");
            Object repos = getRepos.invoke(drm);
            if (repos instanceof RepositoryHandler) {
                return (RepositoryHandler) repos;
            }
            return null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
