package io.github.worldsmiths.spike.ios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Java 8 library APIs that RoboVM 2.3.x's Java 7 runtime does not have. */
final class ExtraProbes {
    static final String[] NAMES = {"java8-function", "java8-optional", "java8-stream", "java8-default-method"};

    private ExtraProbes() {
    }

    static Object run(int probe) {
        switch (probe) {
            case 0:
                Function<String, Integer> length = String::length;
                return length.apply("ok");
            case 1:
                return Optional.of("ok").get();
            case 2:
                return Integer.valueOf(Arrays.asList(1, 2, 3).stream().mapToInt(Integer::intValue).sum());
            case 3:
                List<String> list = new ArrayList<String>(Arrays.asList("b", "a"));
                list.sort(null);
                return list.get(0);
            default:
                throw new IllegalArgumentException();
        }
    }
}
