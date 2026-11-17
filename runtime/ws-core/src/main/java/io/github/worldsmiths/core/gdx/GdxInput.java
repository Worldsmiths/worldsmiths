package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Input;
import io.github.worldsmiths.core.Key;

/** GDX-backed {@link Input}. Not part of the author-facing API — backends assemble this. */
final class GdxInput implements Input {

    @Override
    public boolean isKeyDown(Key key) {
        int code = GdxKeys.code(key);
        return code >= 0 && Gdx.input.isKeyPressed(code);
    }

    @Override
    public boolean isTouched() {
        return Gdx.input.isTouched();
    }

    @Override
    public boolean justTouched() {
        return Gdx.input.justTouched();
    }

    @Override
    public int getX() {
        return Gdx.input.getX();
    }

    @Override
    public int getY() {
        return Gdx.input.getY();
    }
}
