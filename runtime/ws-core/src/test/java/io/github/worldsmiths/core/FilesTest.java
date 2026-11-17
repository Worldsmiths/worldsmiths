package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FilesTest {

    @Test
    void fakeFilesReadsPackagedInternalTextAuthorsSet() {
        FakeEngineServices services = new FakeEngineServices();
        FakeFiles files = services.files();

        files.putInternal("levels/one.txt", "hello");

        assertEquals("hello", files.readString("levels/one.txt"));
    }

    @Test
    void fakeFilesReadsPackagedInternalBytesAuthorsSet() {
        FakeEngineServices services = new FakeEngineServices();
        FakeFiles files = services.files();
        byte[] bytes = new byte[] {1, 2, 3, 4};

        files.putInternal("data.bin", bytes);

        assertArrayEquals(bytes, files.readBytes("data.bin"));
    }

    @Test
    void fakeFilesMissingInternalPathThrowsEngineException() {
        FakeEngineServices services = new FakeEngineServices();
        FakeFiles files = services.files();

        EngineException thrown =
                assertThrows(EngineException.class, () -> files.readString("missing.txt"));
        assertTrue(thrown.getMessage().contains("missing.txt"));

        thrown = assertThrows(EngineException.class, () -> files.readBytes("missing.bin"));
        assertTrue(thrown.getMessage().contains("missing.bin"));
    }

    @Test
    void fakeBagExposesStableFilesInstance() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.files());
        assertSame(services.files(), services.files());
    }

    @Test
    void fakeFilesReadStringUsesUtf8() {
        FakeEngineServices services = new FakeEngineServices();
        FakeFiles files = services.files();
        String text = "café";
        files.putInternal("utf8.txt", text.getBytes(StandardCharsets.UTF_8));

        assertEquals(text, files.readString("utf8.txt"));
    }
}
