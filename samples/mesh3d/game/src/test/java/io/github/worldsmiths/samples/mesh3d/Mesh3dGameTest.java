package io.github.worldsmiths.samples.mesh3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.AssetKind;
import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.LoadState;
import io.github.worldsmiths.core.d3.FakeD3;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Headless mesh path: FakeAssets MESH seed + pump → bind → draw + material tint via {@link FakeD3}.
 */
class Mesh3dGameTest {

    @Test
    void createPumpUpdateBindRenderRecordsMeshDrawAndMaterialToggle() {
        FakeEngineServices services = new FakeEngineServices();
        services.graphics().setSize(800, 600);
        services.assets().putMesh(Mesh3dGame.DEMO_GLTF_PATH);

        Mesh3dGame game = new Mesh3dGame();
        game.create(services);

        assertNotNull(services.scenes().current());
        assertEquals(LoadState.QUEUED, game.meshLoadHandle().state());

        List<io.github.worldsmiths.core.FakeAssets.Command> assetCommands =
                services.assets().commands();
        assertTrue(
                containsAssetRequest(assetCommands, Mesh3dGame.DEMO_GLTF_PATH, AssetKind.MESH));

        services.assets().pump();
        game.update(0f);

        assertEquals(LoadState.COMPLETE, game.meshLoadHandle().state());
        assertTrue(game.isMeshBound(), "expected bind after COMPLETE pump");

        FakeD3 d3 = services.d3();
        assertTrue(
                containsKind(d3.commands(), FakeD3.Command.Kind.CREATE_MODEL_FROM_LOADED),
                "bind should record CREATE_MODEL_FROM_LOADED");

        d3.clear();
        game.render();
        game.update(2.1f);
        game.render();

        List<FakeD3.Command> frame = d3.commands();
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_BEGIN));
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_RENDER));
        assertTrue(containsKind(frame, FakeD3.Command.Kind.BATCH_END));
        assertTrue(
                containsKind(frame, FakeD3.Command.Kind.SET_DIFFUSE)
                        || containsKind(frame, FakeD3.Command.Kind.SET_EMISSIVE),
                "material toggle should record SET_DIFFUSE or SET_EMISSIVE");

        game.dispose();
        assertTrue(d3.batchDisposed());
        assertTrue(d3.modelsDisposed());
    }

    private static boolean containsKind(List<FakeD3.Command> commands, FakeD3.Command.Kind kind) {
        for (FakeD3.Command command : commands) {
            if (command.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAssetRequest(
            List<io.github.worldsmiths.core.FakeAssets.Command> commands,
            String path,
            AssetKind kind) {
        for (io.github.worldsmiths.core.FakeAssets.Command cmd : commands) {
            if (cmd.kind == io.github.worldsmiths.core.FakeAssets.Command.Kind.REQUEST
                    && path.equals(cmd.path)
                    && kind == cmd.assetKind) {
                return true;
            }
        }
        return false;
    }
}