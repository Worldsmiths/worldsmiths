package io.github.worldsmiths.spike.ios;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Java 8 syntax with Java 7 APIs only. Compiled at a higher --release, javac emits
 * newer bytecode for the same source: indy string concatenation (9+) and nestmates (11+).
 */
final class LanguageProbes {
    private LanguageProbes() {
    }

    static String run() {
        final StringBuilder out = new StringBuilder();
        Runnable lambda = () -> out.append("lambda;");
        lambda.run();

        int n = out.length();
        out.append("concat=" + n + "/" + System.nanoTime() % 2 + ";");

        out.append(new Inner().secret()).append(';');

        try (InputStream in = new ByteArrayInputStream(new byte[] {1})) {
            out.append("twr=").append(in.read()).append(';');
        } catch (IOException e) {
            out.append("twr-failed;");
        }

        switch (out.length() > 0 ? "yes" : "no") {
            case "yes":
                out.append("switch-string;");
                break;
            default:
                break;
        }
        return out.toString();
    }

    private static final class Inner {
        private String secret() {
            return "nested-private";
        }
    }
}
