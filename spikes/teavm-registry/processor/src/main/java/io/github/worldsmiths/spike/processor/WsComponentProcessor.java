package io.github.worldsmiths.spike.processor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

/**
 * Generates, for every {@code @WsComponent} class, a {@code <Name>_WsSerializer} implementing libGDX
 * {@code Json.Serializer} with direct field access, plus one {@code GeneratedComponentRegistry} that lists each
 * component's tag, class literal, no-arg factory and serializer. The output contains no reflection.
 */
public final class WsComponentProcessor extends AbstractProcessor {
    static final String ANNOTATION = "io.github.worldsmiths.spike.runtime.WsComponent";
    static final String REGISTRY_PACKAGE_OPTION = "ws.registry.package";
    static final String DEFAULT_REGISTRY_PACKAGE = "io.github.worldsmiths.generated";
    static final String REGISTRY_NAME = "GeneratedComponentRegistry";

    private final List<ComponentModel> components = new ArrayList<>();
    private boolean failed;
    private boolean registryWritten;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ANNOTATION);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(REGISTRY_PACKAGE_OPTION);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement annotation = processingEnv.getElementUtils().getTypeElement(ANNOTATION);
        boolean foundNew = false;
        if (annotation != null) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                foundNew = true;
                ComponentModel model = analyze(element);
                if (model == null) {
                    failed = true;
                    continue;
                }
                components.add(model);
                writeSerializer(model);
            }
        }
        // Written in the round after the last component appears, not in the final round, so sources in the same
        // compilation can reference the registry.
        if (!foundNew && !registryWritten && !failed && !components.isEmpty()) {
            registryWritten = true;
            writeRegistry();
        }
        return true;
    }

    private ComponentModel analyze(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "@WsComponent can only be applied to classes");
            return null;
        }
        TypeElement type = (TypeElement) element;
        String name = type.getQualifiedName().toString();
        boolean ok = true;
        if (!type.getModifiers().contains(Modifier.PUBLIC)) {
            ok = error(type, "@WsComponent class " + name + " must be public");
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            ok = error(type, "@WsComponent class " + name + " must not be abstract");
        }
        if (type.getNestingKind() != NestingKind.TOP_LEVEL && !type.getModifiers().contains(Modifier.STATIC)) {
            ok = error(type, "@WsComponent class " + name + " must be top-level or a static nested class");
        }
        boolean hasNoArg = false;
        for (ExecutableElement ctor : ElementFilter.constructorsIn(type.getEnclosedElements())) {
            if (ctor.getParameters().isEmpty() && ctor.getModifiers().contains(Modifier.PUBLIC)) hasNoArg = true;
        }
        if (!hasNoArg) {
            ok = error(type, "@WsComponent class " + name + " needs a public no-arg constructor");
        }

        List<FieldModel> fields = new ArrayList<>();
        for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
            Set<Modifier> mods = field.getModifiers();
            if (mods.contains(Modifier.STATIC) || mods.contains(Modifier.TRANSIENT)) continue;
            String fieldName = field.getSimpleName().toString();
            if (mods.contains(Modifier.PRIVATE)) {
                ok = error(field, "@WsComponent field '" + fieldName
                        + "' is private; make it package-private or public, or mark it transient");
                continue;
            }
            if (mods.contains(Modifier.FINAL)) {
                ok = error(field, "@WsComponent field '" + fieldName + "' is final; serialized fields must be assignable");
                continue;
            }
            FieldKind kind = FieldKind.of(field.asType());
            if (kind == null) {
                ok = error(field, "@WsComponent field '" + fieldName + "' has unsupported type " + field.asType()
                        + "; supported: boolean, int, long, float, double, String");
                continue;
            }
            fields.add(new FieldModel(fieldName, kind));
        }
        if (!ok) return null;

        String packageName = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
        String binary = processingEnv.getElementUtils().getBinaryName(type).toString();
        String flatName = packageName.isEmpty() ? binary : binary.substring(packageName.length() + 1);
        String serializerName = flatName.replace('$', '_') + "_WsSerializer";
        return new ComponentModel(type, tagOf(type), name, packageName, serializerName, fields);
    }

    private String tagOf(TypeElement type) {
        for (AnnotationMirror mirror : type.getAnnotationMirrors()) {
            if (!mirror.getAnnotationType().toString().equals(ANNOTATION)) continue;
            for (var entry : mirror.getElementValues().entrySet()) {
                if (entry.getKey().getSimpleName().contentEquals("value")) {
                    AnnotationValue value = entry.getValue();
                    String tag = String.valueOf(value.getValue());
                    if (!tag.isEmpty()) return tag;
                }
            }
        }
        return type.getSimpleName().toString();
    }

    private void writeSerializer(ComponentModel c) {
        StringBuilder out = new StringBuilder();
        if (!c.packageName.isEmpty()) out.append("package ").append(c.packageName).append(";\n\n");
        out.append("/** Generated by WsComponentProcessor from ").append(c.className).append(". Do not edit. */\n");
        out.append("@SuppressWarnings(\"rawtypes\")\n");
        out.append("public final class ").append(c.serializerName)
                .append(" implements com.badlogic.gdx.utils.Json.Serializer<").append(c.className).append("> {\n");
        out.append("    @Override\n");
        out.append("    public void write(com.badlogic.gdx.utils.Json json, ").append(c.className)
                .append(" object, Class knownType) {\n");
        out.append("        json.writeObjectStart(").append(c.className).append(".class, knownType);\n");
        for (FieldModel f : c.fields) {
            out.append("        json.writeValue(\"").append(f.name).append("\", object.").append(f.name).append(");\n");
        }
        out.append("        json.writeObjectEnd();\n");
        out.append("    }\n\n");
        out.append("    @Override\n");
        out.append("    public ").append(c.className).append(" read(com.badlogic.gdx.utils.Json json, ")
                .append("com.badlogic.gdx.utils.JsonValue jsonData, Class type) {\n");
        out.append("        ").append(c.className).append(" object = new ").append(c.className).append("();\n");
        if (!c.fields.isEmpty()) out.append("        com.badlogic.gdx.utils.JsonValue value;\n");
        for (FieldModel f : c.fields) {
            out.append("        value = jsonData.get(\"").append(f.name).append("\");\n");
            out.append("        if (value != null) object.").append(f.name).append(" = ").append(f.kind.reader).append(";\n");
        }
        out.append("        return object;\n");
        out.append("    }\n");
        out.append("}\n");
        String qualified = c.packageName.isEmpty() ? c.serializerName : c.packageName + "." + c.serializerName;
        write(qualified, out.toString(), c.element);
    }

    private void writeRegistry() {
        TreeMap<String, ComponentModel> byTag = new TreeMap<>();
        for (ComponentModel c : components) {
            ComponentModel previous = byTag.put(c.tag, c);
            if (previous != null) {
                error(c.element, "duplicate component tag '" + c.tag + "' on " + c.className + " and " + previous.className);
                failed = true;
            }
        }
        if (failed) return;
        String pkg = processingEnv.getOptions().getOrDefault(REGISTRY_PACKAGE_OPTION, DEFAULT_REGISTRY_PACKAGE);

        StringBuilder out = new StringBuilder();
        out.append("package ").append(pkg).append(";\n\n");
        out.append("/** Generated by WsComponentProcessor. Do not edit. */\n");
        out.append("public final class ").append(REGISTRY_NAME)
                .append(" implements io.github.worldsmiths.spike.runtime.ComponentRegistry {\n");
        out.append("    public static final ").append(REGISTRY_NAME).append(" INSTANCE = new ").append(REGISTRY_NAME).append("();\n\n");
        out.append("    private final com.badlogic.gdx.utils.Array<io.github.worldsmiths.spike.runtime.ComponentType<?>> types =\n");
        out.append("            new com.badlogic.gdx.utils.Array<io.github.worldsmiths.spike.runtime.ComponentType<?>>();\n\n");
        out.append("    private ").append(REGISTRY_NAME).append("() {\n");
        for (ComponentModel c : byTag.values()) {
            String serializer = c.packageName.isEmpty() ? c.serializerName : c.packageName + "." + c.serializerName;
            out.append("        types.add(new io.github.worldsmiths.spike.runtime.ComponentType<").append(c.className).append(">(\n");
            out.append("                \"").append(c.tag).append("\",\n");
            out.append("                ").append(c.className).append(".class,\n");
            out.append("                new io.github.worldsmiths.spike.runtime.ComponentFactory<").append(c.className).append(">() {\n");
            out.append("                    @Override\n");
            out.append("                    public ").append(c.className).append(" create() {\n");
            out.append("                        return new ").append(c.className).append("();\n");
            out.append("                    }\n");
            out.append("                },\n");
            out.append("                new ").append(serializer).append("()));\n");
        }
        out.append("    }\n\n");
        out.append("    @Override\n");
        out.append("    public com.badlogic.gdx.utils.Array<io.github.worldsmiths.spike.runtime.ComponentType<?>> types() {\n");
        out.append("        return types;\n");
        out.append("    }\n");
        out.append("}\n");
        Element[] origins = components.stream().map(c -> c.element).toArray(Element[]::new);
        write(pkg + "." + REGISTRY_NAME, out.toString(), origins);
    }

    private void write(String qualifiedName, String source, Element... origins) {
        try (Writer writer = processingEnv.getFiler().createSourceFile(qualifiedName, origins).openWriter()) {
            writer.write(source);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        return false;
    }

    private enum FieldKind {
        BOOLEAN("value.asBoolean()"),
        INT("value.asInt()"),
        LONG("value.asLong()"),
        FLOAT("value.asFloat()"),
        DOUBLE("value.asDouble()"),
        STRING("value.isNull() ? null : value.asString()");

        final String reader;

        FieldKind(String reader) {
            this.reader = reader;
        }

        static FieldKind of(TypeMirror type) {
            switch (type.getKind()) {
                case BOOLEAN: return BOOLEAN;
                case INT: return INT;
                case LONG: return LONG;
                case FLOAT: return FLOAT;
                case DOUBLE: return DOUBLE;
                case DECLARED: return type.toString().equals("java.lang.String") ? STRING : null;
                default: return null;
            }
        }
    }

    private record FieldModel(String name, FieldKind kind) {
    }

    private record ComponentModel(TypeElement element, String tag, String className, String packageName,
                                  String serializerName, List<FieldModel> fields) {
    }
}
