package io.github.worldsmiths.spike.ios;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Java 8 syntax with Java 7 APIs only. Compiled at a higher --release, javac emits
 * newer bytecode for the same source: indy string concatenation (9+) and nestmates (11+).
 */
final class LanguageProbes {
    static final String[] NAMES = {"lambda", "string-concat", "nested-private-access", "try-with-resources", "switch-on-string"};

    private LanguageProbes() {
    }

    static Object run(int probe) throws Exception {
        switch (probe) {
            case 0:
                final StringBuilder out = new StringBuilder();
                Runnable lambda = () -> out.append("ok");
                lambda.run();
                return out;
            case 1:
                int n = (int) (System.nanoTime() % 2);
                return "ok " + n;
            case 2:
                return new Inner().secret();
            case 3:
                try (InputStream in = new ByteArrayInputStream(new byte[] {1})) {
                    return Integer.valueOf(in.read());
                } catch (IOException e) {
                    throw e;
                }
            case 4:
                switch (NAMES[probe]) {
                    case "switch-on-string":
                        return "ok";
                    default:
                        return "wrong branch";
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    private static final class Inner {
        private String secret() {
            return "ok";
        }
    }
}
