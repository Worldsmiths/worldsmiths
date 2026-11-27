package io.github.worldsmiths.samples.minimal;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Blank game for the D1 desktop smoke sample. The engine/backend clears the window;
 * this class only satisfies the {@link WorldsmithsGame} contract.
 */
public final class MinimalGame implements WorldsmithsGame {

    @Override
    public void create(EngineServices services) {
        services.scenes().switchTo(new Scene());
    }

    @Override
    public void update(float delta) {}

    @Override
    public void render() {}

    @Override
    public void dispose() {}
}
