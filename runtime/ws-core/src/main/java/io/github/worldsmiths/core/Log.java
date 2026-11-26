package io.github.worldsmiths.core;

/**
 * Author-facing logging facade from {@link EngineServices#log()}. No LibGDX types. Emits are
 * best-effort: a null message is treated as {@code ""}; implementations must not throw from the
 * emit path for a null message.
 *
 * <p>{@link #error(String, String)} and {@link #error(String, Throwable)} are ambiguous when the
 * second argument is {@code null}. Disambiguate with an explicit cast: {@code error(msg, (String)
 * null)} for a tagged message with no tag value, or {@code error(msg, (Throwable) null)} for a
 * cause-bearing call with no cause. Prefer {@link #error(String, String, Throwable)} when both an
 * author tag and a cause are present. Do not pass a null tag — use the untagged overload instead.
 */
public interface Log {

    void debug(String message);

    void debug(String tag, String message);

    void info(String message);

    void info(String tag, String message);

    void warn(String message);

    void warn(String tag, String message);

    void error(String message);

    /**
     * Error with an author tag. When the second argument would be {@code null}, cast to {@code
     * String} to select this overload over {@link #error(String, Throwable)}; prefer the untagged
     * {@link #error(String)} instead of passing a null tag.
     */
    void error(String tag, String message);

    /**
     * Error with a cause and no author tag. When the second argument would be {@code null}, cast to
     * {@code Throwable} to select this overload over {@link #error(String, String)}.
     */
    void error(String message, Throwable cause);

    /**
     * Error with an author tag and a cause. Prefer this form when both tag and cause are present.
     * Do not pass a null tag — use {@link #error(String, Throwable)} instead.
     */
    void error(String tag, String message, Throwable cause);
}
