package io.github.worldsmiths.samples.mesh3d;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.worldsmiths.core.LoadState;
import io.github.worldsmiths.desktop.DesktopGameListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * Desktop/JVM seam: async MESH must reach COMPLETE, bind, and enable draw — otherwise the listener
 * clears to black and the sample shows a black screen with no visible mesh.
 */
class Mesh3dDesktopBlackScreenRegressionTest {

    @Test
    void jvmDesktopPumpPathBindsMeshForVisibleDraw() throws Exception {
        Mesh3dGame game = new Mesh3dGame();
        DesktopGameListener listener = new DesktopGameListener(game);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicInteger frames = new AtomicInteger();
        AtomicReference<String> blackScreenDetail = new AtomicReference<>();
        final AtomicInteger finishSignals = new AtomicInteger();

        ApplicationListener harness =
                new ApplicationListener() {
                    @Override
                    public void create() {
                        listener.create();
                    }

                    @Override
                    public void render() {
                        listener.render();
                        int n = frames.incrementAndGet();
                        LoadState state = game.meshLoadHandle().state();
                        if (game.isMeshBound()) {
                            signalFinished(blackScreenDetail, finishSignals, finished, null);
                            return;
                        }
                        if (state == LoadState.FAILED) {
                            signalFinished(
                                    blackScreenDetail,
                                    finishSignals,
                                    finished,
                                    "black screen / mesh not drawn: MESH load FAILED — "
                                            + game.meshLoadHandle().failureMessage());
                            return;
                        }
                        if (n >= 180) {
                            signalFinished(
                                    blackScreenDetail,
                                    finishSignals,
                                    finished,
                                    "black screen / mesh not drawn: no visible mesh after "
                                            + n
                                            + " frames; load state="
                                            + state
                                            + ", meshBound=false (COMPLETE→bind→draw never ready)");
                        }
                    }

                    @Override
                    public void resize(int width, int height) {
                        listener.resize(width, height);
                    }

                    @Override
                    public void pause() {
                        listener.pause();
                    }

                    @Override
                    public void resume() {
                        listener.resume();
                    }

                    @Override
                    public void dispose() {
                        listener.dispose();
                    }
                };

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(64, 64);
        config.setTitle("mesh3d-black-screen-regression");

        Thread gl =
                new Thread(
                        () -> new Lwjgl3Application(harness, config),
                        "mesh3d-lwjgl3-harness");
        gl.setDaemon(true);
        gl.start();

        assertTrue(finished.await(45, TimeUnit.SECONDS), "timed out waiting for desktop lifecycle");

        String detail = blackScreenDetail.get();
        if (detail != null) {
            fail(detail);
        }
        assertTrue(game.isMeshBound(), "black screen / mesh not drawn: expected bind after COMPLETE");
        assertEquals(LoadState.COMPLETE, game.meshLoadHandle().state());

        gl.join(15_000L);
    }

    private static void signalFinished(
            AtomicReference<String> blackScreenDetail,
            AtomicInteger finishSignals,
            CountDownLatch finished,
            String failureDetail) {
        if (finishSignals.getAndIncrement() != 0) {
            return;
        }
        if (failureDetail != null) {
            blackScreenDetail.set(failureDetail);
        }
        finished.countDown();
        Gdx.app.exit();
    }
}