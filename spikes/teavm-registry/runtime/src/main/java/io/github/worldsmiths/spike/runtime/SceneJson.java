package io.github.worldsmiths.spike.runtime;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SerializationException;

/**
 * Builds a libGDX {@link Json} whose every type on the scene path has an explicit serializer, so {@code Json} never
 * reaches its reflective fallback (class lookup by name, reflective instantiation, field scanning).
 */
public final class SceneJson {
    public static final String TYPE_FIELD = "type";

    private SceneJson() {
    }

    public static Json create(ComponentRegistry registry) {
        Json json = new Json(JsonWriter.OutputType.json);
        json.setTypeName(TYPE_FIELD);
        ObjectMap<String, ComponentType<?>> byTag = new ObjectMap<String, ComponentType<?>>();
        for (ComponentType<?> type : registry.types()) {
            install(json, type);
            byTag.put(type.tag(), type);
        }
        json.setSerializer(Scene.class, new SceneSerializer());
        json.setSerializer(Node.class, new NodeSerializer(byTag));
        return json;
    }

    private static <T> void install(Json json, ComponentType<T> type) {
        json.addClassTag(type.tag(), type.type());
        json.setSerializer(type.type(), type.serializer());
    }

    @SuppressWarnings("rawtypes")
    private static final class SceneSerializer implements Json.Serializer<Scene> {
        @Override
        public void write(Json json, Scene scene, Class knownType) {
            json.writeObjectStart(Scene.class, knownType);
            json.writeValue("name", scene.name);
            json.writeArrayStart("nodes");
            for (Node node : scene.nodes) json.writeValue(node, Node.class);
            json.writeArrayEnd();
            json.writeObjectEnd();
        }

        @Override
        public Scene read(Json json, JsonValue jsonData, Class type) {
            Scene scene = new Scene(jsonData.getString("name", null));
            JsonValue nodes = jsonData.get("nodes");
            if (nodes != null) {
                for (JsonValue child = nodes.child; child != null; child = child.next) {
                    scene.nodes.add(json.readValue(Node.class, null, child));
                }
            }
            return scene;
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class NodeSerializer implements Json.Serializer<Node> {
        private final ObjectMap<String, ComponentType<?>> byTag;

        NodeSerializer(ObjectMap<String, ComponentType<?>> byTag) {
            this.byTag = byTag;
        }

        @Override
        public void write(Json json, Node node, Class knownType) {
            json.writeObjectStart(Node.class, knownType);
            json.writeValue("name", node.name);
            json.writeArrayStart("components");
            for (Object component : node.components) {
                if (json.getSerializer(component.getClass()) == null) {
                    throw new SerializationException("Component " + component.getClass().getName()
                            + " is not in the generated registry; annotate it with @WsComponent");
                }
                json.writeValue(component, null);
            }
            json.writeArrayEnd();
            json.writeArrayStart("children");
            for (Node child : node.children) json.writeValue(child, Node.class);
            json.writeArrayEnd();
            json.writeObjectEnd();
        }

        @Override
        public Node read(Json json, JsonValue jsonData, Class type) {
            Node node = new Node(jsonData.getString("name", null));
            JsonValue components = jsonData.get("components");
            if (components != null) {
                for (JsonValue value = components.child; value != null; value = value.next) {
                    String tag = value.getString(TYPE_FIELD, null);
                    ComponentType<?> componentType = tag == null ? null : byTag.get(tag);
                    if (componentType == null) throw new SerializationException("Unknown component type '" + tag + "'");
                    node.components.add(json.readValue(componentType.type(), null, value));
                }
            }
            JsonValue children = jsonData.get("children");
            if (children != null) {
                for (JsonValue child = children.child; child != null; child = child.next) {
                    node.children.add(json.readValue(Node.class, null, child));
                }
            }
            return node;
        }
    }
}
