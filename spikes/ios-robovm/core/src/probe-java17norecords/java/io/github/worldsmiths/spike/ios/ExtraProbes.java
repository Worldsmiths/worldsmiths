package io.github.worldsmiths.spike.ios;

/** Java 14 to 17 language features without records, plus one Java 17 API. Needs javaRelease >= 17. */
final class ExtraProbes {
    static final String[] NAMES = {
        "java14-switch-expression", "java15-text-block", "java16-pattern-instanceof", "java17-sealed-types", "java17-class-issealed-api"
    };

    private ExtraProbes() {
    }

    sealed interface Shape permits Circle, Square {
    }

    static final class Circle implements Shape {
        final int r = 1;
    }

    static final class Square implements Shape {
        final int side = 3;
    }

    static Object run(int probe) {
        switch (probe) {
            case 0:
                return switch (NAMES[probe].length() % 2) {
                    case 0 -> "even";
                    default -> "odd";
                };
            case 1:
                return """
                    ok""";
            case 2:
                Object o = "ok";
                if (o instanceof String s) {
                    return s;
                }
                return "no match";
            case 3:
                Shape shape = new Square();
                return shape instanceof Square sq ? Integer.valueOf(sq.side) : Integer.valueOf(-1);
            case 4:
                return Boolean.valueOf(Shape.class.isSealed());
            default:
                throw new IllegalArgumentException();
        }
    }
}
