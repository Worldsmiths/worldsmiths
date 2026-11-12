package io.github.worldsmiths.spike.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import io.github.worldsmiths.spike.runtime.ComponentRegistry;
import io.github.worldsmiths.spike.runtime.ComponentType;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WsComponentProcessorTest {
    private static final String REGISTRY_OPTION = "-Aws.registry.package=demo.gen";

    @TempDir
    Path work;

    private static final String ALL_TYPES = """
            package demo;
            import io.github.worldsmiths.spike.runtime.WsComponent;
            @WsComponent
            public class AllTypes {
                public boolean flag = true;
                public int count = 7;
                public long big = 1L;
                public float ratio = 0.25f;
                public double precise = 2.5;
                public String label = "default";
                public static int ignoredStatic = 3;
                public transient int ignoredTransient = 4;
            }
            """;

    private static final String MARKER = """
            package demo.other;
            import io.github.worldsmiths.spike.runtime.WsComponent;
            @WsComponent("Mark")
            public class Marker {
                int level;
            }
            """;

    private static Map<String, String> sources(String... nameAndSource) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < nameAndSource.length; i += 2) map.put(nameAndSource[i], nameAndSource[i + 1]);
        return map;
    }

    private Compilation compileOk(Map<String, String> sources) {
        Compilation c = Compilation.compile(work, sources, REGISTRY_OPTION);
        assertTrue(c.success, () -> "compilation failed:\n" + c.errors());
        return c;
    }

    private static ComponentRegistry registry(Compilation c) throws Exception {
        Class<?> registryClass = c.classLoader().loadClass("demo.gen.GeneratedComponentRegistry");
        return (ComponentRegistry) registryClass.getField("INSTANCE").get(null);
    }

    private static Json jsonFor(ComponentRegistry registry) {
        Json json = new Json(JsonWriter.OutputType.json);
        json.setTypeName("type");
        for (ComponentType<?> type : registry.types()) install(json, type);
        return json;
    }

    private static <T> void install(Json json, ComponentType<T> type) {
        json.addClassTag(type.tag(), type.type());
        json.setSerializer(type.type(), type.serializer());
    }

    private static Object field(Object target, String name) throws Exception {
        return target.getClass().getField(name).get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        target.getClass().getField(name).set(target, value);
    }

    @Test
    void registryListsEveryComponentSortedByTagWithClassLiteralAndFactory() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES, "demo.other.Marker", MARKER));
        ComponentRegistry registry = registry(c);
        Array<ComponentType<?>> types = registry.types();

        assertEquals(2, types.size);
        assertEquals("AllTypes", types.get(0).tag());
        assertEquals("Mark", types.get(1).tag());
        Class<?> allTypes = c.classLoader().loadClass("demo.AllTypes");
        assertEquals(allTypes.getName(), types.get(0).type().getName());

        Object first = types.get(0).create();
        Object second = types.get(0).create();
        assertEquals("demo.AllTypes", first.getClass().getName());
        assertNotSame(first, second);
        assertEquals(7, field(first, "count"));
    }

    @Test
    void registryInstanceIsASingletonAndTypesAreStable() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES));
        ComponentRegistry registry = registry(c);
        assertSame(registry.types(), registry.types());
    }

    @Test
    void generatedSerializerRoundTripsEverySupportedFieldType() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES));
        ComponentRegistry registry = registry(c);
        Json json = jsonFor(registry);

        Object original = registry.types().get(0).create();
        setField(original, "flag", false);
        setField(original, "count", -42);
        setField(original, "big", 9_000_000_000L);
        setField(original, "ratio", 12.75f);
        setField(original, "precise", 3.141592653589793);
        setField(original, "label", "hero \"quoted\"");

        String text = json.toJson(original, (Class<?>) null);
        assertTrue(text.contains("\"type\":\"AllTypes\""), text);
        assertFalse(text.contains("ignoredStatic"), text);
        assertFalse(text.contains("ignoredTransient"), text);

        Object copy = json.readValue((Class<?>) null, null, new JsonReader().parse(text));
        assertEquals(original.getClass(), copy.getClass());
        assertEquals(false, field(copy, "flag"));
        assertEquals(-42, field(copy, "count"));
        assertEquals(9_000_000_000L, field(copy, "big"));
        assertEquals(12.75f, field(copy, "ratio"));
        assertEquals(3.141592653589793, field(copy, "precise"));
        assertEquals("hero \"quoted\"", field(copy, "label"));
    }

    @Test
    void nullStringsRoundTripAsNull() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES));
        ComponentRegistry registry = registry(c);
        Json json = jsonFor(registry);
        Object original = registry.types().get(0).create();
        setField(original, "label", null);

        Object copy = json.readValue((Class<?>) null, null, new JsonReader().parse(json.toJson(original, (Class<?>) null)));

        assertNull(field(copy, "label"));
    }

    @Test
    void missingJsonFieldsKeepConstructorDefaults() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES));
        Json json = jsonFor(registry(c));

        JsonValue partial = new JsonReader().parse("{\"type\":\"AllTypes\",\"count\":99}");
        Object copy = json.readValue((Class<?>) null, null, partial);

        assertEquals(99, field(copy, "count"));
        assertEquals(true, field(copy, "flag"));
        assertEquals("default", field(copy, "label"));
        assertEquals(0.25f, field(copy, "ratio"));
    }

    @Test
    void packagePrivateFieldsAreSerializedBecauseTheSerializerLivesInTheComponentPackage() throws Exception {
        Compilation c = compileOk(sources("demo.other.Marker", MARKER));
        String serializer = c.generatedSource("demo.other.Marker_WsSerializer");
        assertTrue(serializer.contains("\"level\""), serializer);
    }

    @Test
    void generatedCodeUsesNoReflection() throws Exception {
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES, "demo.other.Marker", MARKER));
        String all = c.generatedSource("demo.gen.GeneratedComponentRegistry")
                + c.generatedSource("demo.AllTypes_WsSerializer")
                + c.generatedSource("demo.other.Marker_WsSerializer");
        for (String banned : new String[] {"forName", "reflect", "getDeclared", "newInstance", "getField", "getMethod"}) {
            assertFalse(all.contains(banned), "generated code contains " + banned + ":\n" + all);
        }
    }

    @Test
    void sourcesInTheSameCompilationCanReferenceTheRegistry() throws Exception {
        String user = """
                package demo;
                public class Boot {
                    public static int count() { return demo.gen.GeneratedComponentRegistry.INSTANCE.types().size; }
                }
                """;
        Compilation c = compileOk(sources("demo.AllTypes", ALL_TYPES, "demo.Boot", user));
        assertEquals(1, c.classLoader().loadClass("demo.Boot").getMethod("count").invoke(null));
    }

    @Test
    void registryPackageDefaultsWhenOptionIsAbsent() throws Exception {
        Compilation c = Compilation.compile(work, sources("demo.AllTypes", ALL_TYPES));
        assertTrue(c.success, c::errors);
        c.classLoader().loadClass("io.github.worldsmiths.generated.GeneratedComponentRegistry");
    }

    private void assertRejected(String source, String expectedMessage) {
        Compilation c = Compilation.compile(work, sources("demo.Bad", source), REGISTRY_OPTION);
        assertFalse(c.success, "expected compilation to fail");
        assertTrue(c.errors().contains(expectedMessage), () -> "errors were:\n" + c.errors());
    }

    private static String bad(String classModifiers, String body) {
        return "package demo;\nimport io.github.worldsmiths.spike.runtime.WsComponent;\n@WsComponent\n"
                + classModifiers + " class Bad {\n" + body + "\n}\n";
    }

    @Test
    void rejectsPrivateFields() {
        assertRejected(bad("public", "private int hidden;"), "field 'hidden' is private");
    }

    @Test
    void rejectsFinalFields() {
        assertRejected(bad("public", "public final int fixed = 1;"), "field 'fixed' is final");
    }

    @Test
    void rejectsUnsupportedFieldTypes() {
        assertRejected(bad("public", "public java.util.List<String> items;"), "field 'items' has unsupported type");
    }

    @Test
    void rejectsMissingPublicNoArgConstructor() {
        assertRejected(bad("public", "public Bad(int x) {}"), "needs a public no-arg constructor");
    }

    @Test
    void rejectsNonPublicClasses() {
        assertRejected(bad("", "public int x;"), "must be public");
    }

    @Test
    void rejectsAbstractClasses() {
        assertRejected(bad("public abstract", "public int x;"), "must not be abstract");
    }

    @Test
    void rejectsDuplicateTags() {
        String a = "package demo;\nimport io.github.worldsmiths.spike.runtime.WsComponent;\n@WsComponent(\"Same\")\npublic class A { public int x; }\n";
        String b = "package demo;\nimport io.github.worldsmiths.spike.runtime.WsComponent;\n@WsComponent(\"Same\")\npublic class B { public int y; }\n";
        Compilation c = Compilation.compile(work, sources("demo.A", a, "demo.B", b), REGISTRY_OPTION);
        assertFalse(c.success);
        assertTrue(c.errors().contains("duplicate component tag 'Same'"), c::errors);
    }
}
