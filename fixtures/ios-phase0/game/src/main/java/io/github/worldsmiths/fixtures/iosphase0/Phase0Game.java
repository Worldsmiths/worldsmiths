package io.github.worldsmiths.fixtures.iosphase0;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/** Minimal game for the pre-Snake RoboVM-in-generated Phase-0 package proof. */
public final class Phase0Game implements WorldsmithsGame {

    @Override
    public Scene createScene() {
        return new Scene();
    }

    @Override
    public void create(EngineServices services) {}

    @Override
    public void update(float delta) {}

    @Override
    public void render() {}

    @Override
    public void dispose() {}
}
