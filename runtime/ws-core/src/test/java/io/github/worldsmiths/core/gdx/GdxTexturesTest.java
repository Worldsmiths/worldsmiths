package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.EngineException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxTextures coverage via an injected {@link GdxTextures.TextureOpener} — no Application
 * / display / GL context required.
 */
class GdxTexturesTest {

    @Test
    void missingInternalPathThrowsEngineException() {
        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                return null;
                            }
                        });

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.preload("gone.png"));
        assertTrue(thrown.getMessage().contains("gone.png"));

        thrown = assertThrows(EngineException.class, () -> textures.resolve("gone.png"));
        assertTrue(thrown.getMessage().contains("gone.png"));
    }

    @Test
    void unloadableOpenThrowsEngineException() {
        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                throw new IllegalStateException("decode failed");
                            }
                        });

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.preload("bad.png"));
        assertTrue(thrown.getMessage().contains("bad.png"));
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void secondPreloadSameKeyOpensOnce() {
        AtomicInteger opens = new AtomicInteger();
        final GdxTextures.LoadedTexture stub =
                new GdxTextures.LoadedTexture() {
                    @Override
                    public Texture gdxTexture() {
                        return null;
                    }

                    @Override
                    public void dispose() {}
                };
        Map<String, GdxTextures.LoadedTexture> catalog =
                new HashMap<String, GdxTextures.LoadedTexture>();
        catalog.put("hero.png", stub);

        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                opens.incrementAndGet();
                                return catalog.get(path);
                            }
                        });

        textures.preload("hero.png");
        textures.preload("hero.png");
        textures.resolve("hero.png");

        assertEquals(1, opens.get());
    }

    @Test
    void requireReturnsCachedLoadedTexture() {
        final GdxTextures.LoadedTexture stub =
                new GdxTextures.LoadedTexture() {
                    @Override
                    public Texture gdxTexture() {
                        return null;
                    }

                    @Override
                    public void dispose() {}
                };

        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                return stub;
                            }
                        });

        textures.preload("hero.png");
        assertEquals(null, textures.require("hero.png"));
    }
}
