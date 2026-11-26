package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class FakeLogTest {

    @Test
    void recordsOrderedInfoThenWarnWithLevelAndMessage() {
        FakeLog log = new FakeLog();

        log.info("booting");
        log.warn("missing asset");

        java.util.List<FakeLog.Command> commands = log.commands();
        assertEquals(2, commands.size());
        assertEquals(FakeLog.Command.Level.INFO, commands.get(0).level);
        assertEquals("booting", commands.get(0).message);
        assertEquals(FakeLog.Command.Level.WARN, commands.get(1).level);
        assertEquals("missing asset", commands.get(1).message);
    }

    @Test
    void untaggedCallRecordsNullAuthorTag() {
        FakeLog log = new FakeLog();

        log.info("booting");

        assertNull(log.commands().get(0).tag);
    }

    @Test
    void taggedCallRecordsThatTag() {
        FakeLog log = new FakeLog();

        log.info("Sprites", "booting");

        assertEquals("Sprites", log.commands().get(0).tag);
        assertEquals("booting", log.commands().get(0).message);
    }

    @Test
    void errorWithCauseRecordsCauseIdentity() {
        FakeLog log = new FakeLog();
        Throwable cause = new RuntimeException("boom");

        log.error("failed", cause);

        assertEquals(FakeLog.Command.Level.ERROR, log.commands().get(0).level);
        assertEquals("failed", log.commands().get(0).message);
        assertSame(cause, log.commands().get(0).cause);
    }

    @Test
    void nullMessageRecordsEmptyStringAndDoesNotThrow() {
        FakeLog log = new FakeLog();

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

        java.util.List<FakeLog.Command> commands = log.commands();
        assertEquals(10, commands.size());
        for (FakeLog.Command command : commands) {
            assertEquals("", command.message);
        }
    }
}