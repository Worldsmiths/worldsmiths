package io.github.worldsmiths.gradle;

/** Escapes a string for embedding inside a double-quoted Java or Kotlin literal. */
final class JavaStringEscapes {

    private JavaStringEscapes() {}

    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Escapes a string for embedding inside an XML attribute value. */
    static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
