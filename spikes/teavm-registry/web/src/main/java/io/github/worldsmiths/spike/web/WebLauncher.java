package io.github.worldsmiths.spike.web;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;

public final class WebLauncher {
    private WebLauncher() {
    }

    public static void main(String[] args) {
        WebApplicationConfiguration config = new WebApplicationConfiguration();
        config.width = 320;
        config.height = 120;
        new WebApplication(new SceneViewer(), config);
    }
}
