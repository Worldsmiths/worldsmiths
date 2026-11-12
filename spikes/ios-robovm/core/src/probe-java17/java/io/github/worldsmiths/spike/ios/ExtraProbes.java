package io.github.worldsmiths.spike.ios;

/** Java 14 to 17 language features, plus one Java 17 API. Needs javaRelease >= 17. */
final class ExtraProbes {
    static final String[] NAMES = {
        "java16-record-accessor", "java16-record-tostring-equals", "java14-switch-expression", "java15-text-block",
        "java16-pattern-instanceof", "java17-sealed-types", "java17-class-issealed-api"
    };

    private ExtraProbes() {
    }

    record Point(int x, int y) {
    }

    sealed interface Shape permits Circle, Square {
    }

    record Circle(int r) implements Shape {
    }

    record Square(int side) implements Shape {
    }

    static Object run(int probe) {
        switch (probe) {
            case 0:
                return Integer.valueOf(new Point(1, 2).y());
            case 1:
                Point p = new Point(1, 2);
                return p.equals(new Point(1, 2)) ? p.toString() : "not equal";
            case 2:
                return switch (NAMES[probe].length() % 2) {
                    case 0 -> "even";
                    default -> "odd";
                };
            case 3:
                return """
                    ok""";
            case 4:
                Object o = "ok";
                if (o instanceof String s) {
                    return s;
                }
                return "no match";
            case 5:
                Shape shape = new Square(3);
                return shape instanceof Square sq ? Integer.valueOf(sq.side()) : Integer.valueOf(-1);
            case 6:
                return Boolean.valueOf(Shape.class.isSealed());
            default:
                throw new IllegalArgumentException();
        }
    }
}
