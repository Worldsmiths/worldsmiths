package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FakeAssetsTest {

    @Test
    void seededTextureRequestReachesCompleteAfterPumpWithRequestThenCompleteRecords() {
        FakeAssets assets = new FakeAssets();
        assets.putTexture("textures/heavy.png");

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);

        assertNotNull(handle);
        assertEquals(LoadState.QUEUED, handle.state());
        assertEquals("textures/heavy.png", handle.path());
        assertEquals(AssetKind.TEXTURE, handle.kind());

        assets.pump();

        assertEquals(LoadState.COMPLETE, handle.state());
        assertEquals(1f, handle.progress(), 1e-5f);
        assertNull(handle.failureMessage());

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(2, commands.size());
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals("textures/heavy.png", commands.get(0).path);
        assertEquals(AssetKind.TEXTURE, commands.get(0).assetKind);
        assertEquals(FakeAssets.Command.Kind.COMPLETE, commands.get(1).kind);
        assertEquals("textures/heavy.png", commands.get(1).path);
    }

    @Test
    void missingTextureRequestDoesNotThrowAndReachesFailedWithPathAfterPump() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request("textures/missing.png", AssetKind.TEXTURE);

        assertNotNull(handle);
        assertEquals(LoadState.QUEUED, handle.state());

        assets.pump();

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().contains("textures/missing.png"));

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(2, commands.size());
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
        assertEquals("textures/missing.png", commands.get(1).path);
        assertTrue(commands.get(1).message.contains("textures/missing.png"));
    }

    @Test
    void scriptedMultiStepVisitsQueuedLoadingThenCompleteWithDiscreteProgress() {
        FakeAssets assets = new FakeAssets();
        assets.putTexture("textures/heavy.png");
        assets.script("textures/heavy.png", 0.5f);

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);

        assertEquals(LoadState.QUEUED, handle.state());
        assertEquals(0f, handle.progress(), 1e-5f);

        assets.pump();
        assertEquals(LoadState.LOADING, handle.state());
        assertEquals(0.5f, handle.progress(), 1e-5f);

        assets.pump();
        assertEquals(LoadState.COMPLETE, handle.state());
        assertEquals(1f, handle.progress(), 1e-5f);

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(3, commands.size());
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.PROGRESS, commands.get(1).kind);
        assertEquals(0.5f, commands.get(1).progress, 1e-5f);
        assertEquals(FakeAssets.Command.Kind.COMPLETE, commands.get(2).kind);
    }

    @Test
    void onTerminalFiresAfterPumpBeforeUpdateMarkerOnComplete() {
        FakeAssets assets = new FakeAssets();
        assets.putTexture("textures/heavy.png");

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        final boolean[] fired = {false};
        handle.onTerminal(
                new Runnable() {
                    @Override
                    public void run() {
                        fired[0] = true;
                    }
                });

        assertFalse(fired[0]);
        assets.pump();
        assertTrue(fired[0]);
        assertEquals(LoadState.COMPLETE, handle.state());

        assets.markUpdate();

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(4, commands.size());
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.COMPLETE, commands.get(1).kind);
        assertEquals(FakeAssets.Command.Kind.LISTENER, commands.get(2).kind);
        assertEquals(FakeAssets.Command.Kind.UPDATE, commands.get(3).kind);
    }

    @Test
    void onTerminalFiresAfterPumpBeforeUpdateMarkerOnFailed() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request("textures/missing.png", AssetKind.TEXTURE);
        final boolean[] fired = {false};
        handle.onTerminal(
                new Runnable() {
                    @Override
                    public void run() {
                        fired[0] = true;
                    }
                });

        assets.pump();
        assertTrue(fired[0]);
        assertEquals(LoadState.FAILED, handle.state());
        assets.markUpdate();

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
        assertEquals(FakeAssets.Command.Kind.LISTENER, commands.get(2).kind);
        assertEquals(FakeAssets.Command.Kind.UPDATE, commands.get(3).kind);
    }

    @Test
    void meshRequestReachesFailedWithUnsupportedKindMessage() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request("models/hero.mesh", AssetKind.MESH);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().toLowerCase().contains("unsupported"));
        assertTrue(handle.failureMessage().contains("MESH"));

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
    }

    @Test
    void musicRequestReachesFailedWithUnsupportedKindMessage() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request("audio/theme.ogg", AssetKind.MUSIC);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());
        assertTrue(handle.failureMessage().toLowerCase().contains("unsupported"));
        assertTrue(handle.failureMessage().contains("MUSIC"));

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
    }

    @Test
    void reRequestSameKindAndPathSharesInFlightHandle() {
        FakeAssets assets = new FakeAssets();
        assets.putTexture("textures/heavy.png");

        LoadHandle first = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        LoadHandle second = assets.request("textures/heavy.png", AssetKind.TEXTURE);

        assertSame(first, second);
        assertEquals(LoadState.QUEUED, first.state());

        assets.pump();
        assertEquals(LoadState.COMPLETE, first.state());
        assertEquals(LoadState.COMPLETE, second.state());
    }

    @Test
    void reRequestAfterCompleteReturnsSameCompletedHandle() {
        FakeAssets assets = new FakeAssets();
        assets.putTexture("textures/heavy.png");

        LoadHandle first = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.COMPLETE, first.state());

        LoadHandle second = assets.request("textures/heavy.png", AssetKind.TEXTURE);

        assertSame(first, second);
        assertEquals(LoadState.COMPLETE, second.state());
    }

    @Test
    void reRequestAfterFailedStartsFreshLoadThatCanCompleteIndependently() {
        FakeAssets assets = new FakeAssets();

        LoadHandle first = assets.request("textures/retry.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.FAILED, first.state());

        assets.putTexture("textures/retry.png");
        LoadHandle second = assets.request("textures/retry.png", AssetKind.TEXTURE);

        assertNotSame(first, second);
        assertEquals(LoadState.QUEUED, second.state());
        assertEquals(LoadState.FAILED, first.state());

        assets.pump();
        assertEquals(LoadState.COMPLETE, second.state());
        assertEquals(LoadState.FAILED, first.state());
    }

    @Test
    void nullPathReachesFailedDeterministically() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request(null, AssetKind.TEXTURE);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
    }

    @Test
    void blankPathReachesFailedDeterministically() {
        FakeAssets assets = new FakeAssets();

        LoadHandle handle = assets.request("   ", AssetKind.TEXTURE);

        assertEquals(LoadState.FAILED, handle.state());
        assertNotNull(handle.failureMessage());

        java.util.List<FakeAssets.Command> commands = assets.commands();
        assertEquals(FakeAssets.Command.Kind.REQUEST, commands.get(0).kind);
        assertEquals(FakeAssets.Command.Kind.FAIL, commands.get(1).kind);
    }

    @Test
    void completeMergesIntoFakeTexturesSoResolveDoesNotCreateTwice() {
        FakeEngineServices services = new FakeEngineServices();
        FakeAssets assets = services.assets();
        FakeTextures textures = services.textures();
        assets.putTexture("textures/heavy.png");

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assets.pump();
        assertEquals(LoadState.COMPLETE, handle.state());

        textures.resolve("textures/heavy.png");

        int creates = 0;
        for (FakeTextures.Command command : textures.commands()) {
            if (command.kind == FakeTextures.Command.Kind.CREATE) {
                creates++;
            }
        }
        assertEquals(1, creates);
        assertEquals(FakeTextures.Command.Kind.CREATE, textures.commands().get(0).kind);
        assertEquals("textures/heavy.png", textures.commands().get(0).path);
        assertEquals(FakeTextures.Command.Kind.RESOLVE, textures.commands().get(1).kind);
    }

    @Test
    void syncPreloadWhileAsyncTextureInFlightThrowsThenResolveWorksAfterComplete() {
        FakeEngineServices services = new FakeEngineServices();
        FakeAssets assets = services.assets();
        FakeTextures textures = services.textures();
        assets.putTexture("textures/heavy.png");

        LoadHandle handle = assets.request("textures/heavy.png", AssetKind.TEXTURE);
        assertEquals(LoadState.QUEUED, handle.state());

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.preload("textures/heavy.png"));
        assertTrue(thrown.getMessage().toLowerCase().contains("in flight"));
        assertTrue(thrown.getMessage().contains("textures/heavy.png"));
        assertTrue(textures.commands().isEmpty());

        assets.pump();
        assertEquals(LoadState.COMPLETE, handle.state());

        textures.resolve("textures/heavy.png");
        assertEquals(FakeTextures.Command.Kind.CREATE, textures.commands().get(0).kind);
        assertEquals(FakeTextures.Command.Kind.RESOLVE, textures.commands().get(1).kind);
    }
}
