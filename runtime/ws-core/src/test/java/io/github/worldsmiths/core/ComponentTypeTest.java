package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class ComponentTypeTest {

    @Test
    void exposesTagTypeFactoryAndSerializer() {
        Json.Serializer<Transform2D> serializer = noopSerializer();
        ComponentType<Transform2D> type = new ComponentType<Transform2D>(
                "Transform2D",
                Transform2D.class,
                new ComponentFactory<Transform2D>() {
                    @Override
                    public Transform2D create() {
                        return new Transform2D();
                    }
                },
                serializer);

        assertEquals("Transform2D", type.tag());
        assertSame(Transform2D.class, type.type());
        assertSame(serializer, type.serializer());

        Transform2D first = type.create();
        Transform2D second = type.create();
        assertNotNull(first);
        assertNotSame(first, second);
    }

    @Test
    void serializerIsNotPartOfThePublicAuthorApi() throws Exception {
        Method serializer = ComponentType.class.getDeclaredMethod("serializer");
        assertFalse(Modifier.isPublic(serializer.getModifiers()));
        assertFalse(Modifier.isProtected(serializer.getModifiers()));
    }

    private static Json.Serializer<Transform2D> noopSerializer() {
        return new Json.Serializer<Transform2D>() {
            @Override
            public void write(Json json, Transform2D object, Class knownType) {}

            @Override
            public Transform2D read(Json json, JsonValue jsonData, Class type) {
                return null;
            }
        };
    }
}
