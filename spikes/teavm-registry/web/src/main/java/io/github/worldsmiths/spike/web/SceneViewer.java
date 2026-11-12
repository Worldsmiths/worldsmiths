package io.github.worldsmiths.spike.web;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.worldsmiths.spike.game.DemoScene;
import io.github.worldsmiths.spike.game.Transform2D;

/** Runs the scene round-trip inside the TeaVM-compiled libGDX app and publishes the result to the page. */
public final class SceneViewer extends ApplicationAdapter {
    static final String MARKER = "WS-S2-RESULT: ";

    private boolean passed;

    @Override
    public void create() {
        StringBuilder report = new StringBuilder();
        try {
            DemoScene.RoundTrip result = DemoScene.roundTrip();
            passed = result.passed();
            report.append(MARKER).append(passed ? "PASS" : "FAIL").append('\n');
            report.append("platform: ").append(Gdx.app.getType()).append('\n');
            report.append("deserialized scene:\n").append(DemoScene.describe(result.decoded));
            for (String failure : result.failures) report.append("mismatch: ").append(failure).append('\n');
            report.append("json: ").append(result.json).append('\n');
            report.append("control: ").append(reflectiveControl()).append('\n');
        } catch (Throwable t) {
            passed = false;
            report.append(MARKER).append("FAIL\n").append("exception: ").append(t).append('\n');
        }
        Gdx.app.log("WS-S2", report.toString());
        BrowserPage.show(passed, report.toString());
    }

    /**
     * Negative control: the same component read through a plain libGDX Json with no registry, which takes Json's
     * reflective path. With gdx-teavm reflection disabled this must fail, showing the PASS above did not use reflection.
     */
    private static String reflectiveControl() {
        try {
            Transform2D t = new Json().fromJson(Transform2D.class, "{\"x\":5}");
            return "reflective Json read succeeded x=" + t.x;
        } catch (Throwable t) {
            return "reflective Json failed as expected (" + t.getClass().getName() + ": " + t.getMessage() + ")";
        }
    }

    @Override
    public void render() {
        if (passed) {
            ScreenUtils.clear(0.1f, 0.6f, 0.2f, 1f);
        } else {
            ScreenUtils.clear(0.7f, 0.1f, 0.1f, 1f);
        }
    }
}
