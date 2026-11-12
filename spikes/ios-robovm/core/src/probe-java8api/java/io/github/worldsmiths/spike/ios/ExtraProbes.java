package io.github.worldsmiths.spike.ios;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/** Java 8 library APIs that RoboVM 2.3.x's Java 7 runtime does not have. */
final class ExtraProbes {
    private ExtraProbes() {
    }

    static String run() {
        Function<String, Integer> length = String::length;
        Optional<String> value = Optional.of("java8api");
        int sum = Arrays.asList(1, 2, 3).stream().mapToInt(Integer::intValue).sum();
        return value.map(length).orElse(-1) + "/" + sum;
    }
}
