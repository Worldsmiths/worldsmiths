package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
