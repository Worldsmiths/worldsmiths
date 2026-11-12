package io.github.worldsmiths.spike.ios;

import java.util.List;
import java.util.Optional;

/** Java 10/11 syntax and APIs. Needs javaRelease >= 11. */
final class ExtraProbes {
    private ExtraProbes() {
    }

    static String run() {
        var list = List.of("a", "b", "c");
        var copy = List.copyOf(list);
        var blank = " ".isBlank();
        return copy.size() + "/" + blank + "/" + Optional.empty().isEmpty();
    }
}
