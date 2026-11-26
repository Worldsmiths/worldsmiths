package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.worldsmiths.core.EngineServices;
import io.github.worldsmiths.core.RectDraw;
import java.util.List;

/**
 * Shared retained-scene draw path for backend {@code SceneShapeRenderer}s. Null/empty texture keys
 * paint filled rgba rects via {@link ShapeRenderer}; set keys paint tinted textured quads via {@link
 * SpriteBatch} using the engine {@link GdxTextures} cache. Async in-flight texture keys are skipped
 * (no sync {@code require}/opener). Ends one batcher before beginning the other. Not part of the
 * author-facing API.
 */
public final class GdxRetainedSceneDraw {
    private final ShapeBatch shapes;
    private final SpriteBatcher sprites;
    private final TextureLookup textures;
    private boolean disposed;

    /**
     * Production drawer bound to the {@link GdxTextures} cache inside a bag from {@link
     * GdxEngineServices#createCooperative()} or {@link GdxJvmEngineServices#create()}.
     */
    public static GdxRetainedSceneDraw fromEngineServices(EngineServices services) {
        return new GdxRetainedSceneDraw(GdxEngineServices.requireGdxTextures(services));
    }

    /** Production path: owns ShapeRenderer + SpriteBatch; resolves textures via {@code textures}. */
    GdxRetainedSceneDraw(GdxTextures textures) {
        this(
                new GdxShapeBatch(new ShapeRenderer()),
                new GdxSpriteBatcher(new SpriteBatch()),
                texturesLookup(textures));
    }

    /** Package-visible for headless tests that inject recording seams. */
    GdxRetainedSceneDraw(ShapeBatch shapes, SpriteBatcher sprites, TextureLookup textures) {
        this.shapes = shapes;
        this.sprites = sprites;
        this.textures = textures;
    }

    /**
     * Draws {@code commands} in screen space using {@code projection}. Empty list is a no-op.
     * Callers must end any open {@link io.github.worldsmiths.core.Draw2D} batch before this runs
     * (listener order: game.render then scene draw).
     */
    public void draw(List<RectDraw> commands, Matrix4 projection) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        boolean shapesOpen = false;
        boolean spritesOpen = false;
        try {
            for (RectDraw command : commands) {
                if (command.textureKey == null) {
                    if (spritesOpen) {
                        sprites.end();
                        spritesOpen = false;
                    }
                    if (!shapesOpen) {
                        shapes.setProjectionMatrix(projection);
                        shapes.beginFilled();
                        shapesOpen = true;
                    }
                    shapes.setColor(command.r, command.g, command.b, command.a);
                    shapes.rect(command.x, command.y, command.width, command.height);
                } else {
                    // In-flight async keys: skip textured draw — never sync-require / opener.open.
                    if (textures.isAsyncInFlight(command.textureKey)) {
                        continue;
                    }
                    if (shapesOpen) {
                        shapes.end();
                        shapesOpen = false;
                    }
                    if (!spritesOpen) {
                        sprites.setProjectionMatrix(projection);
                        sprites.begin();
                        spritesOpen = true;
                    }
                    Texture texture = textures.require(command.textureKey);
                    sprites.setColor(command.r, command.g, command.b, command.a);
                    sprites.draw(texture, command.x, command.y, command.width, command.height);
                }
            }
        } finally {
            if (shapesOpen) {
                shapes.end();
            }
            if (spritesOpen) {
                sprites.end();
            }
        }
    }

    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        shapes.dispose();
        sprites.dispose();
    }

    /** Narrow ShapeRenderer surface so tests avoid constructing a live GL ShapeRenderer. */
    interface ShapeBatch {
        void setProjectionMatrix(Matrix4 matrix);

        void beginFilled();

        void setColor(float r, float g, float b, float a);

        void rect(float x, float y, float width, float height);

        void end();

        void dispose();

        boolean isDrawing();
    }

    /** Narrow SpriteBatch surface so tests avoid constructing a live GL SpriteBatch. */
    interface SpriteBatcher {
        void setProjectionMatrix(Matrix4 matrix);

        void begin();

        void setColor(float r, float g, float b, float a);

        void draw(Texture texture, float x, float y, float width, float height);

        void end();

        void dispose();

        boolean isDrawing();
    }

    /**
     * Cache lookup used for textured commands — typically {@link GdxTextures#require(String)}.
     * {@link #isAsyncInFlight(String)} lets draw skip keys that {@link GdxAssets} is still loading.
     */
    interface TextureLookup {
        Texture require(String path);

        /** When true, retained draw skips the textured command (no sync open). */
        boolean isAsyncInFlight(String path);
    }

    private static TextureLookup texturesLookup(final GdxTextures textures) {
        return new TextureLookup() {
            @Override
            public Texture require(String path) {
                return textures.require(path);
            }

            @Override
            public boolean isAsyncInFlight(String path) {
                return textures.isAsyncInFlight(path);
            }
        };
    }

    private static final class GdxShapeBatch implements ShapeBatch {
        private final ShapeRenderer shapes;

        GdxShapeBatch(ShapeRenderer shapes) {
            this.shapes = shapes;
        }

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {
            shapes.setProjectionMatrix(matrix);
        }

        @Override
        public void beginFilled() {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            shapes.setColor(r, g, b, a);
        }

        @Override
        public void rect(float x, float y, float width, float height) {
            shapes.rect(x, y, width, height);
        }

        @Override
        public void end() {
            shapes.end();
        }

        @Override
        public void dispose() {
            shapes.dispose();
        }

        @Override
        public boolean isDrawing() {
            return shapes.isDrawing();
        }
    }

    private static final class GdxSpriteBatcher implements SpriteBatcher {
        private final SpriteBatch batch;

        GdxSpriteBatcher(SpriteBatch batch) {
            this.batch = batch;
        }

        @Override
        public void setProjectionMatrix(Matrix4 matrix) {
            batch.setProjectionMatrix(matrix);
        }

        @Override
        public void begin() {
            batch.begin();
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            batch.setColor(r, g, b, a);
        }

        @Override
        public void draw(Texture texture, float x, float y, float width, float height) {
            batch.draw(texture, x, y, width, height);
        }

        @Override
        public void end() {
            batch.end();
        }

        @Override
        public void dispose() {
            batch.dispose();
        }

        @Override
        public boolean isDrawing() {
            return batch.isDrawing();
        }
    }
}
