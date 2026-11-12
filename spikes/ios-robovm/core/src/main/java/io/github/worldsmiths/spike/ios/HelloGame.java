package io.github.worldsmiths.spike.ios;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

public class HelloGame extends ApplicationAdapter {
    @Override
    public void create() {
        Gdx.app.log("S3", "language probes: " + LanguageProbes.run());
        Gdx.app.log("S3", "extra probes: " + ExtraProbes.run());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.2f, 0.35f, 1f);
    }
}
