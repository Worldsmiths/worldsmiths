package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

    @Test
    void listsRegisteredComponentTypes() {
        final ComponentType<Transform2D> transform = type(
                "Transform2D", Transform2D.class, new ComponentFactory<Transform2D>() {
                    @Override
                    public Transform2D create() {
                        return new Transform2D();
                    }
                });
        final ComponentType<Sprite> sprite = type(
                "Sprite", Sprite.class, new ComponentFactory<Sprite>() {
                    @Override
                    public Sprite create() {
                        return new Sprite();
                    }
                });

        ComponentRegistry registry = new ComponentRegistry() {
            @Override
            public Array<ComponentType<?>> types() {
                Array<ComponentType<?>> types = new Array<ComponentType<?>>();
                types.add(transform);
                types.add(sprite);
                return types;
            }
        };

        Array<ComponentType<?>> types = registry.types();
        assertEquals(2, types.size);
        assertSame(transform, types.get(0));
        assertSame(sprite, types.get(1));
        assertEquals("Transform2D", types.get(0).tag());
        assertEquals("Sprite", types.get(1).tag());
        assertSame(Transform2D.class, types.get(0).type());
        assertSame(Sprite.class, types.get(1).type());
    }

    @Test
    void generatedRegistryTypesAreNotALiveMutableArray() {
        ComponentRegistry registry = GeneratedComponentRegistry.INSTANCE;
        Array<ComponentType<?>> first = registry.types();
        int originalSize = first.size;
        first.clear();
        assertEquals(originalSize, registry.types().size);
        assertEquals(0, first.size);
    }

    @Test
    void combineUnionsComponentTypesFromEachRegistry() {
        ComponentType<Transform2D> transform = type(
                "Transform2D", Transform2D.class, new ComponentFactory<Transform2D>() {
                    @Override
                    public Transform2D create() {
                        return new Transform2D();
                    }
                });
        ComponentType<Sprite> sprite = type(
                "Sprite", Sprite.class, new ComponentFactory<Sprite>() {
                    @Override
                    public Sprite create() {
                        return new Sprite();
                    }
                });

        ComponentRegistry left = singleton(transform);
        ComponentRegistry right = singleton(sprite);

        ComponentRegistry combined = ComponentRegistry.combine(left, right);
        Array<ComponentType<?>> types = combined.types();
        assertEquals(2, types.size);
        assertSame(transform, types.get(0));
        assertSame(sprite, types.get(1));

        types.clear();
        assertEquals(2, combined.types().size);
    }

    @Test
    void builtinsExposesEngineGeneratedRegistry() {
        ComponentRegistry builtins = ComponentRegistries.builtins();
        assertSame(GeneratedComponentRegistry.INSTANCE, builtins);
        Array<ComponentType<?>> types = builtins.types();
        assertEquals(3, types.size);
        assertEquals("Sprite", types.get(0).tag());
        assertEquals("Transform2D", types.get(1).tag());
        assertEquals("Transform3D", types.get(2).tag());
    }

    @Test
    void combineRejectsDuplicateTags() {
        ComponentType<Transform2D> a = type(
                "Transform2D", Transform2D.class, new ComponentFactory<Transform2D>() {
                    @Override
                    public Transform2D create() {
                        return new Transform2D();
                    }
                });
        ComponentType<Transform2D> b = type(
                "Transform2D", Transform2D.class, new ComponentFactory<Transform2D>() {
                    @Override
                    public Transform2D create() {
                        return new Transform2D();
                    }
                });

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> ComponentRegistry.combine(singleton(a), singleton(b)));
        assertTrue(e.getMessage().contains("Transform2D"), e.getMessage());
    }

    private static ComponentRegistry singleton(final ComponentType<?> type) {
        return new ComponentRegistry() {
            @Override
            public Array<ComponentType<?>> types() {
                Array<ComponentType<?>> types = new Array<ComponentType<?>>();
                types.add(type);
                return types;
            }
        };
    }

    private static <T> ComponentType<T> type(String tag, Class<T> clazz, ComponentFactory<T> factory) {
        return new ComponentType<T>(tag, clazz, factory, new Json.Serializer<T>() {
            @Override
            public void write(Json json, T object, Class knownType) {}

            @Override
            public T read(Json json, JsonValue jsonData, Class type) {
                return null;
            }
        });
    }
}
