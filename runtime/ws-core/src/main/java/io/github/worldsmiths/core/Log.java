package io.github.worldsmiths.core;

/**
 * Author-facing logging facade from {@link EngineServices#log()}. No LibGDX types. Emits are
 * best-effort: a null message is treated as {@code ""}; implementations must not throw from the
 * emit path for a null message.
 */
public interface Log {

    void debug(String message);

    void debug(String tag, String message);

    void info(String message);

    void info(String tag, String message);

    void warn(String message);

    void warn(String tag, String message);

    void error(String message);

    void error(String tag, String message);

    void error(String message, Throwable cause);

    void error(String tag, String message, Throwable cause);
}
