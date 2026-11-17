package io.github.worldsmiths.samples.minimal;

import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Blank game for the D1 desktop smoke sample. The engine/backend clears the window;
 * this class only satisfies the {@link WorldsmithsGame} contract.
 */
public final class MinimalGame implements WorldsmithsGame {

    @Override
    public Scene createScene() {
        return new Scene();
    }

    @Override
    public void create() {}

    @Override
    public void update(float delta) {}

    @Override
    public void render() {}

    @Override
    public void dispose() {}
}
