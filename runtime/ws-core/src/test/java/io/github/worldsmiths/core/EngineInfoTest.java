package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class EngineInfoTest {

    @Test
    void reportsTheGitHubGroupId() {
        assertEquals("io.github.worldsmiths", EngineInfo.group());
    }

    @Test
    void isCompiledForJava8() throws IOException {
        try (DataInputStream in = new DataInputStream(
                EngineInfo.class.getResourceAsStream("EngineInfo.class"))) {
            assertEquals(0xCAFEBABE, in.readInt());
            in.readUnsignedShort();
            assertEquals(52, in.readUnsignedShort(), "class file major version 52 is Java 8");
        }
    }
}
