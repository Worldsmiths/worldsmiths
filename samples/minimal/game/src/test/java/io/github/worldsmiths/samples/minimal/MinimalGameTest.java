package io.github.worldsmiths.samples.minimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.worldsmiths.core.FakeEngineServices;
import org.junit.jupiter.api.Test;

class MinimalGameTest {

    @Test
    void createSwitchesToNonNullCurrentScene() {
        FakeEngineServices services = new FakeEngineServices();
        MinimalGame game = new MinimalGame();
        game.create(services);
        assertNotNull(services.scenes().current());
    }
}
