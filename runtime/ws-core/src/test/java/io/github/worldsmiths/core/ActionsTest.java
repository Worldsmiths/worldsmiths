package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ActionsTest {

    @Test
    void justPressedIsTrueOnlyOnTransitionFrameThenHeldIsDownOnly() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("jump", Controls.key(Key.SPACE));

        actions.setKeyDown(Key.SPACE, true);
        actions.tick();
        assertTrue(actions.justPressed("jump"));
        assertTrue(actions.isDown("jump"));

        actions.tick();
        assertFalse(actions.justPressed("jump"));
        assertTrue(actions.isDown("jump"));
    }

    @Test
    void unknownPollIsSoftFalseAndBlankOrNullMutateThrows() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        assertFalse(actions.isDown("no_such_action"));
        assertFalse(actions.justPressed("no_such_action"));
        assertFalse(actions.isDown(null));
        assertFalse(actions.justPressed(""));

        assertThrows(EngineException.class, () -> actions.bind(null, Controls.key(Key.A)));
        assertThrows(EngineException.class, () -> actions.bind("  ", Controls.key(Key.A)));
        assertThrows(EngineException.class, () -> actions.bind("move", (Control) null));
        assertThrows(
                EngineException.class,
                () -> actions.bind("move", Controls.key(Key.A), null));
    }

    @Test
    void multiBindOrThenReplaceAllDropsOtherControl() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("move_up", Controls.key(Key.UP), Controls.key(Key.W));

        actions.setKeyDown(Key.UP, true);
        actions.tick();
        assertTrue(actions.isDown("move_up"));

        actions.setKeyDown(Key.UP, false);
        actions.setKeyDown(Key.W, true);
        actions.tick();
        assertTrue(actions.isDown("move_up"));

        actions.bind("move_up", Controls.key(Key.W));
        actions.setKeyDown(Key.W, false);
        actions.setKeyDown(Key.UP, true);
        actions.tick();
        assertFalse(actions.isDown("move_up"));

        actions.setKeyDown(Key.UP, false);
        actions.setKeyDown(Key.W, true);
        actions.tick();
        assertTrue(actions.isDown("move_up"));
    }

    @Test
    void uiAcceptRespondsViaFakeDevicePath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.setKeyDown(Key.ENTER, true);
        actions.tick();
        assertTrue(actions.justPressed("ui_accept"));
        assertTrue(actions.isDown("ui_accept"));

        actions.setKeyDown(Key.ENTER, false);
        actions.tick();
        assertFalse(actions.isDown("ui_accept"));

        actions.setKeyDown(Key.SPACE, true);
        actions.tick();
        assertTrue(actions.isDown("ui_accept"));
    }

    @Test
    void pointerButtonBindIsIndependentOfKeyAndPad() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("fire", Controls.pointerButton(PointerButton.PRIMARY));

        actions.setKeyDown(Key.SPACE, true);
        actions.setPadButtonDown(PadButton.A, true);
        actions.tick();
        assertFalse(actions.isDown("fire"));

        actions.setKeyDown(Key.SPACE, false);
        actions.setPadButtonDown(PadButton.A, false);
        actions.setPointerButtonDown(PointerButton.PRIMARY, true);
        actions.tick();
        assertTrue(actions.isDown("fire"));
        assertTrue(actions.justPressed("fire"));
    }

    @Test
    void missingPadBindSucceedsAndPollStaysFalseUntilPadSet() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("confirm", Controls.gamepadButton(PadButton.A));
        actions.tick();
        assertFalse(actions.isDown("confirm"));
        assertFalse(actions.justPressed("confirm"));

        actions.setPadButtonDown(PadButton.A, true);
        actions.tick();
        assertTrue(actions.isDown("confirm"));
        assertTrue(actions.justPressed("confirm"));
    }

    @Test
    void bagActionsIsNonNullAndIdentityStable() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.actions());
        assertSame(services.actions(), services.actions());
        assertTrue(services.actions() instanceof FakeActions);
    }

    @Test
    void setActionActiveIsFakeOnlyShortcutNotSoleDeviceProof() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("dash", Controls.key(Key.D));
        actions.setActionActive("dash", true);
        actions.tick();
        assertTrue(actions.isDown("dash"));
        assertTrue(actions.justPressed("dash"));

        // Device path still required for real Control proofs — key alone works without shortcut.
        actions.setActionActive("dash", false);
        actions.tick();
        assertFalse(actions.isDown("dash"));

        actions.setKeyDown(Key.D, true);
        actions.tick();
        assertTrue(actions.isDown("dash"));
    }

    @Test
    void inputSetKeyDownFeedsSameDevicePortAsActions() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();

        actions.bind("left", Controls.key(Key.LEFT));
        services.input().setKeyDown(Key.LEFT, true);
        actions.tick();
        assertTrue(actions.isDown("left"));
        assertTrue(services.input().isKeyDown(Key.LEFT));
    }

    @Test
    void loadFromFakeFilesAppliesBindingsViaDevicePath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();
        FakeFiles files = services.files();
        files.putInternal(
                "input/bindings.txt",
                "# packaged bindings\n"
                        + "move_up key W\n"
                        + "move_up pad DPAD_UP\n"
                        + "click pointer PRIMARY\n");

        actions.load(files, "input/bindings.txt");

        actions.setKeyDown(Key.W, true);
        actions.tick();
        assertTrue(actions.isDown("move_up"));

        actions.setKeyDown(Key.W, false);
        actions.tick();
        assertFalse(actions.isDown("move_up"));

        actions.setPadButtonDown(PadButton.DPAD_UP, true);
        actions.tick();
        assertTrue(actions.isDown("move_up"));

        actions.setPadButtonDown(PadButton.DPAD_UP, false);
        actions.setPointerButtonDown(PointerButton.PRIMARY, true);
        actions.tick();
        assertTrue(actions.isDown("click"));
    }

    @Test
    void loadMissingPathThrowsAndLeavesExistingBindingsIntact() {
        FakeEngineServices services = new FakeEngineServices();
        FakeActions actions = services.actions();
        actions.bind("keep", Controls.key(Key.K));

        assertThrows(
                EngineException.class,
                () -> actions.load(services.files(), "input/missing.txt"));

        actions.setKeyDown(Key.K, true);
        actions.tick();
        assertTrue(actions.isDown("keep"));
    }
}
