package io.github.worldsmiths.samples.asyncload;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.LoadHandle;
import io.github.worldsmiths.core.LoadState;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Async heavy-texture sample: requests {@code textures/heavy.png} via {@link
 * EngineServices#assets()} and emits load signals through {@link EngineServices#log()} (sample-only;
 * the engine stays quiet on FAILED). Game sources stay free of LibGDX types.
 */
public final class AsyncLoadGame implements WorldsmithsGame {

    static final String HEAVY_PATH = "textures/heavy.png";
    static final String LOG_TAG = "async";

    private LoadHandle loadHandle;
    private EngineServices services;
    private boolean terminalLogged;

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
        services.scenes().switchTo(new Scene("asyncload"));
        services.graphics().setTitle("AsyncLoad");
        loadHandle = services.assets().request(HEAVY_PATH, AssetKind.TEXTURE);
        services.log().info(LOG_TAG, "loading " + HEAVY_PATH);
    }

    @Override
    public void update(float delta) {
        if (services == null || loadHandle == null || terminalLogged) {
            return;
        }
        LoadState state = loadHandle.state();
        if (state == LoadState.COMPLETE) {
            services.log().info(LOG_TAG, "complete: " + loadHandle.path());
            terminalLogged = true;
        } else if (state == LoadState.FAILED) {
            String detail = loadHandle.failureMessage();
            if (detail == null || detail.isEmpty()) {
                detail = "FAILED: " + loadHandle.path();
            }
            services.log().warn(LOG_TAG, detail);
            terminalLogged = true;
        }
    }

    @Override
    public void render() {
        // No game-owned draw; packaging + log observation is the sample focus.
    }

    /** Package-visible for headless tests that assert load state. */
    LoadHandle loadHandle() {
        return loadHandle;
    }
}
