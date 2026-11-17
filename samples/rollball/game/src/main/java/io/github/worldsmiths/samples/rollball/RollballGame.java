package io.github.worldsmiths.samples.rollball;

import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * D8 rollball stub: empty scene that compiles on all four packaging targets.
 * Gameplay, ModelBatch, and follow camera land in T3.
 */
public final class RollballGame implements WorldsmithsGame {

    @Override
    public Scene createScene() {
        return new Scene("rollball");
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
