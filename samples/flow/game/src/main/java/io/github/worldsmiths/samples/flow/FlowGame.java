package io.github.worldsmiths.samples.flow;

import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.WorldsmithsGame;

/**
 * Multi-scene flow sample: stub menu → gameplay → second SceneCodec room via {@link
 * EngineServices#scenes()}. SPACE (or ENTER / primary touch) advances one hop per rising edge. Game
 * sources stay free of LibGDX types.
 */
public final class FlowGame implements WorldsmithsGame {

    static final String MENU_PATH = "scenes/menu.json";
    static final String GAMEPLAY_PATH = "scenes/gameplay.json";
    static final String ROOM_B_PATH = "scenes/room_b.json";

    private EngineServices services;
    private boolean advanceHeld;

    @Override
    public void create(EngineServices services) {
        if (services == null) {
            throw new NullPointerException("services");
        }
        this.services = services;
        services.scenes().load(MENU_PATH);
        services.graphics().setTitle("Flow");
        services.log().info("Flow: stub menu — SPACE/ENTER to play");
    }

    @Override
    public void update(float delta) {
        if (services == null) {
            return;
        }
        boolean wantAdvance = wantsAdvance(services.input());
        if (wantAdvance && !advanceHeld) {
            hopForward();
        }
        advanceHeld = wantAdvance;
    }

    @Override
    public void render() {
        // Retained scene is drawn by the backend SceneShapeRenderer.
    }

    private void hopForward() {
        Scene current = services.scenes().current();
        if (current == null) {
            return;
        }
        String name = current.name;
        if ("Menu".equals(name)) {
            services.scenes().load(GAMEPLAY_PATH);
            services.log().info("Flow: gameplay — SPACE/ENTER for room B");
        } else if ("Gameplay".equals(name)) {
            services.scenes().load(ROOM_B_PATH);
            services.log().info("Flow: room B (end of stub hops)");
        }
    }

    private static boolean wantsAdvance(Input input) {
        return input.isKeyDown(Key.SPACE)
                || input.isKeyDown(Key.ENTER)
                || input.justTouched();
    }
}
