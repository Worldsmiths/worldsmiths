package io.github.worldsmiths.spike.ios;

import java.io.PrintStream;

/**
 * Runs every probe in isolation and prints one line per probe. Uses no string
 * concatenation or lambdas itself, so it still reports when those features break.
 */
public final class ProbeReport {
    private ProbeReport() {
    }

    public static void print(PrintStream out) {
        for (int i = 0; i < LanguageProbes.NAMES.length; i++) {
            line(out, LanguageProbes.NAMES[i]);
            try {
                out.println(String.valueOf(LanguageProbes.run(i)));
            } catch (Throwable t) {
                failure(out, t);
            }
        }
        for (int i = 0; i < ExtraProbes.NAMES.length; i++) {
            line(out, ExtraProbes.NAMES[i]);
            try {
                out.println(String.valueOf(ExtraProbes.run(i)));
            } catch (Throwable t) {
                failure(out, t);
            }
        }
        out.println("S3PROBE done");
        out.flush();
    }

    private static void line(PrintStream out, String name) {
        // Flush per probe so a native crash in one probe still leaves the earlier lines.
        out.flush();
        out.print("S3PROBE ");
        out.print(name);
        out.print(": ");
        out.flush();
    }

    private static void failure(PrintStream out, Throwable t) {
        out.print("FAILED ");
        out.print(t.getClass().getName());
        out.print(' ');
        out.println(t.getMessage());
    }
}
