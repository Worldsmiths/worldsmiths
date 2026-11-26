package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Texture;
import io.github.worldsmiths.core.EngineException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        // Same LoadedTexture instance preload put in the cache (not a null==null vacuous pass).
        assertSame(stub, textures.requireLoaded("hero.png"));
        assertSame(stub.gdxTexture(), textures.require("hero.png"));
    }

    @Test
    void syncPreloadWhileAsyncInFlightThrowsWithoutOpening() {
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
        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                opens.incrementAndGet();
                                return stub;
                            }
                        });

        textures.beginAsyncLoad("hero.png");

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.preload("hero.png"));
        assertTrue(thrown.getMessage().toLowerCase().contains("async"));
        assertTrue(thrown.getMessage().contains("hero.png"));
        assertEquals(0, opens.get());

        thrown = assertThrows(EngineException.class, () -> textures.resolve("hero.png"));
        assertTrue(thrown.getMessage().toLowerCase().contains("async"));
        assertEquals(0, opens.get());
    }

    @Test
    void disposeDisposesCachedTexturesAndClearsCache() {
        final List<String> disposed = new ArrayList<String>();
        final AtomicInteger opens = new AtomicInteger();
        GdxTextures textures =
                new GdxTextures(
                        new GdxTextures.TextureOpener() {
                            @Override
                            public GdxTextures.LoadedTexture open(String path) {
                                opens.incrementAndGet();
                                final String key = path;
                                return new GdxTextures.LoadedTexture() {
                                    @Override
                                    public Texture gdxTexture() {
                                        return null;
                                    }

                                    @Override
                                    public void dispose() {
                                        disposed.add(key);
                                    }
                                };
                            }
                        });

        textures.preload("hero.png");
        textures.preload("tile.png");
        assertEquals(2, opens.get());

        textures.dispose();
        textures.dispose(); // idempotent — second call must not dispose again

        assertEquals(2, disposed.size());
        assertTrue(disposed.contains("hero.png"));
        assertTrue(disposed.contains("tile.png"));

        textures.preload("hero.png");
        assertEquals(3, opens.get());
    }
}
