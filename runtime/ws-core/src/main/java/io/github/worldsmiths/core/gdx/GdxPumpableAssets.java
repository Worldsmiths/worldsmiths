package io.github.worldsmiths.core.gdx;

import io.github.worldsmiths.core.Assets;

/**
 * Package-private pump/dispose surface shared by cooperative and AssetManager-backed GDX {@link
 * Assets} implementations. Not part of the author API.
 */
interface GdxPumpableAssets extends Assets {
    void pump();

    void dispose();
}
