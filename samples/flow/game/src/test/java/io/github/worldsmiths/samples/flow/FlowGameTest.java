package io.github.worldsmiths.samples.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.FakeScenes;
import io.github.worldsmiths.core.Key;
import io.github.worldsmiths.core.Scene;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless multi-scene flow: FakeFiles seed + FakeInput drives stub-menu → gameplay → room_b;
 * FakeScenes must show ≥2 successful replaces after the initial load.
 */
class FlowGameTest {

    static final String MENU_JSON =
            "{\"schemaVersion\":1,\"name\":\"Menu\",\"nodes\":["
                    + "{\"name\":\"title\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":0.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":null,\"r\":0.2,\"g\":0.2,\"b\":0.5,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]}"
                    + "]}";

    static final String GAMEPLAY_JSON =
            "{\"schemaVersion\":1,\"name\":\"Gameplay\",\"nodes\":["
                    + "{\"name\":\"player\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":1.0,\"y\":1.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":null,\"r\":0.2,\"g\":0.8,\"b\":0.2,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]}"
                    + "]}";

    static final String ROOM_B_JSON =
            "{\"schemaVersion\":1,\"name\":\"RoomB\",\"nodes\":["
                    + "{\"name\":\"door\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":2.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":null,\"r\":0.8,\"g\":0.4,\"b\":0.1,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]}"
                    + "]}";

    @Test
    void fakeInputDrivesMenuToGameplayToRoomWithTwoReplacesAfterInitialLoad() {
        FakeEngineServices services = seededServices();
        FlowGame game = new FlowGame();
        game.create(services);

        Scene afterCreate = services.scenes().current();
        assertNotNull(afterCreate, "create must load the stub menu");
        assertEquals("Menu", afterCreate.name);

        int exitsAfterCreate = countKind(services.scenes().commands(), FakeScenes.Command.Kind.EXIT);

        // Menu → gameplay (press + release frames so rising-edge latch clears)
        pressAdvance(services, game);
        assertEquals("Gameplay", services.scenes().current().name);

        // Gameplay → second codec room
        pressAdvance(services, game);

        Scene current = services.scenes().current();
        assertNotNull(current);
        assertEquals("RoomB", current.name);

        List<FakeScenes.Command> commands = services.scenes().commands();
        int exitsAfterHops = countKind(commands, FakeScenes.Command.Kind.EXIT);
        int replacesAfterInitial = exitsAfterHops - exitsAfterCreate;
        assertTrue(
                replacesAfterInitial >= 2,
                "expected ≥2 successful replaces after initial load, got "
                        + replacesAfterInitial
                        + ": "
                        + commands);
        assertTrue(
                containsLoad(commands, FlowGame.GAMEPLAY_PATH),
                "expected LOAD " + FlowGame.GAMEPLAY_PATH + ": " + commands);
        assertTrue(
                containsLoad(commands, FlowGame.ROOM_B_PATH),
                "expected LOAD " + FlowGame.ROOM_B_PATH + ": " + commands);
    }

    private static void pressAdvance(FakeEngineServices services, FlowGame game) {
        services.input().setKeyDown(Key.SPACE, true);
        game.update(0f);
        services.input().setKeyDown(Key.SPACE, false);
        game.update(0f);
    }

    private static FakeEngineServices seededServices() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal(FlowGame.MENU_PATH, MENU_JSON);
        services.files().putInternal(FlowGame.GAMEPLAY_PATH, GAMEPLAY_JSON);
        services.files().putInternal(FlowGame.ROOM_B_PATH, ROOM_B_JSON);
        return services;
    }

    private static int countKind(List<FakeScenes.Command> commands, FakeScenes.Command.Kind kind) {
        int count = 0;
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == kind) {
                count += 1;
            }
        }
        return count;
    }

    private static boolean containsLoad(List<FakeScenes.Command> commands, String path) {
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == FakeScenes.Command.Kind.LOAD && path.equals(cmd.path)) {
                return true;
            }
        }
        return false;
    }
}
