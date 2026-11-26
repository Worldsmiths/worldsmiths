package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Headless GdxLog coverage via an injected {@link GdxLog.Sink} — no live Application required for
 * mapping asserts. LOG_DEBUG + wire coverage uses package-visible {@link
 * GdxEngineServices#createLog(AppLogging)} (narrow Application logging seam; full {@code create()}
 * still needs GL for Draw2D).
 */
class GdxLogTest {

    @Test
    void untaggedInfoAndErrorUseDefaultSinkTagWs() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.info("booting");
        log.error("failed");

        assertEquals(2, sink.calls.size());
        assertEquals("log", sink.calls.get(0).channel);
        assertEquals("WS", sink.calls.get(0).tag);
        assertEquals("booting", sink.calls.get(0).message);
        assertEquals("error", sink.calls.get(1).channel);
        assertEquals("WS", sink.calls.get(1).tag);
        assertEquals("failed", sink.calls.get(1).message);
    }

    @Test
    void taggedOverloadsPassAuthorTagThrough() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.debug("Sprites", "tick");
        log.info("Sprites", "booting");
        log.warn("Sprites", "missing");
        log.error("Sprites", "failed");

        assertEquals("Sprites", sink.calls.get(0).tag);
        assertEquals("Sprites", sink.calls.get(1).tag);
        assertEquals("Sprites", sink.calls.get(2).tag);
        assertEquals("Sprites", sink.calls.get(3).tag);
    }

    @Test
    void nullAuthorTagNormalizesToWsOnSink() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.debug(null, "tick");
        log.info(null, "booting");
        log.warn(null, "missing");
        log.error((String) null, "failed");
        log.error(null, "failed", new RuntimeException("x"));

        assertEquals(5, sink.calls.size());
        assertEquals("WS", sink.calls.get(0).tag);
        assertEquals("WS", sink.calls.get(1).tag);
        assertEquals("WS", sink.calls.get(2).tag);
        assertEquals("WS", sink.calls.get(3).tag);
        assertEquals("WS", sink.calls.get(4).tag);
    }

    @Test
    void blankAuthorTagNormalizesToWsOnSink() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.debug("", "tick");
        log.info("   ", "booting");
        log.warn("\t", "missing");
        log.error(" ", "failed");
        log.error("", "failed", new RuntimeException("x"));

        assertEquals(5, sink.calls.size());
        assertEquals("WS", sink.calls.get(0).tag);
        assertEquals("WS", sink.calls.get(1).tag);
        assertEquals("WS", sink.calls.get(2).tag);
        assertEquals("WS", sink.calls.get(3).tag);
        assertEquals("WS", sink.calls.get(4).tag);
    }

    @Test
    void warnMapsToInfoChannelWithWarnPrefix() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.warn("missing");
        log.warn("Audio", "clip gone");

        assertEquals(2, sink.calls.size());
        assertEquals("log", sink.calls.get(0).channel);
        assertEquals("WS", sink.calls.get(0).tag);
        assertTrue(sink.calls.get(0).message.startsWith("WARN: "));
        assertEquals("WARN: missing", sink.calls.get(0).message);
        assertEquals("log", sink.calls.get(1).channel);
        assertEquals("Audio", sink.calls.get(1).tag);
        assertEquals("WARN: clip gone", sink.calls.get(1).message);
    }

    @Test
    void errorWithThrowablePassesCauseIdentityToSink() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);
        Throwable cause = new RuntimeException("boom");

        log.error("failed", cause);
        log.error("Sprites", "failed", cause);

        assertEquals(2, sink.calls.size());
        assertEquals("error", sink.calls.get(0).channel);
        assertEquals("WS", sink.calls.get(0).tag);
        assertEquals("failed", sink.calls.get(0).message);
        assertSame(cause, sink.calls.get(0).exception);
        assertEquals("Sprites", sink.calls.get(1).tag);
        assertSame(cause, sink.calls.get(1).exception);
    }

    @Test
    void nullMessageSinksEmptyStringAndDoesNotThrow() {
        RecordingSink sink = new RecordingSink();
        GdxLog log = new GdxLog(sink);

        log.debug(null);
        log.info(null);
        log.warn(null);
        log.error((String) null);
        log.error((String) null, new RuntimeException("x"));
        log.debug("T", null);
        log.info("T", null);
        log.warn("T", null);
        log.error("T", (String) null);
        log.error("T", null, new RuntimeException("y"));

        assertEquals(10, sink.calls.size());
        assertEquals("", sink.calls.get(0).message);
        assertEquals("", sink.calls.get(1).message);
        assertEquals("WARN: ", sink.calls.get(2).message);
        assertEquals("", sink.calls.get(3).message);
        assertEquals("", sink.calls.get(4).message);
        assertEquals("", sink.calls.get(5).message);
        assertEquals("", sink.calls.get(6).message);
        assertEquals("WARN: ", sink.calls.get(7).message);
        assertEquals("", sink.calls.get(8).message);
        assertEquals("", sink.calls.get(9).message);
    }

    @Test
    void createLogSetsLogDebugAndWiresNonNullGdxLog() {
        AtomicInteger lastLevel = new AtomicInteger(-1);
        RecordingSink sink = new RecordingSink();
        GdxEngineServices.AppLogging appLogging =
                new GdxEngineServices.AppLogging() {
                    @Override
                    public void setLogLevel(int logLevel) {
                        lastLevel.set(logLevel);
                    }

                    @Override
                    public GdxLog.Sink sink() {
                        return sink;
                    }
                };

        GdxLog log = GdxEngineServices.createLog(appLogging);

        assertEquals(Application.LOG_DEBUG, lastLevel.get());
        assertNotNull(log);
        log.info("booting");
        assertEquals(1, sink.calls.size());
        assertEquals("WS", sink.calls.get(0).tag);
        assertEquals("booting", sink.calls.get(0).message);
    }

    private static final class RecordingSink implements GdxLog.Sink {
        final List<Call> calls = new ArrayList<Call>();

        @Override
        public void debug(String tag, String message) {
            calls.add(new Call("debug", tag, message, null));
        }

        @Override
        public void log(String tag, String message) {
            calls.add(new Call("log", tag, message, null));
        }

        @Override
        public void error(String tag, String message) {
            calls.add(new Call("error", tag, message, null));
        }

        @Override
        public void error(String tag, String message, Throwable exception) {
            calls.add(new Call("error", tag, message, exception));
        }
    }

    private static final class Call {
        final String channel;
        final String tag;
        final String message;
        final Throwable exception;

        Call(String channel, String tag, String message, Throwable exception) {
            this.channel = channel;
            this.tag = tag;
            this.message = message;
            this.exception = exception;
        }
    }
}
