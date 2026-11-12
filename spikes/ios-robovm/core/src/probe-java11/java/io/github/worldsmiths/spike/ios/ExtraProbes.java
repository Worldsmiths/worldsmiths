package io.github.worldsmiths.spike.ios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Java 8 to 11 syntax and APIs. Needs javaRelease >= 11. */
final class ExtraProbes {
    static final String[] NAMES = {
        "java8-function", "java8-stream", "java9-list-of", "java10-var-and-copy-of", "java11-string-isblank", "java11-optional-isempty"
    };

    private ExtraProbes() {
    }

    static Object run(int probe) {
        switch (probe) {
            case 0:
                Function<String, Integer> length = String::length;
                return length.apply("ok");
            case 1:
                return Integer.valueOf(List.of(1, 2, 3).stream().mapToInt(Integer::intValue).sum());
            case 2:
                return List.of("a", "b").get(1);
            case 3:
                var copy = List.copyOf(List.of("ok"));
                return copy.get(0);
            case 4:
                return Boolean.valueOf(" ".isBlank());
            case 5:
                return Boolean.valueOf(Optional.empty().isEmpty());
            default:
                throw new IllegalArgumentException();
        }
    }
}
