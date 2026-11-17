package io.github.worldsmiths.core.gdx;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import io.github.worldsmiths.core.EngineException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Headless GdxFiles coverage via an injected {@link GdxFiles.InternalOpener} — no Application /
 * display required.
 */
class GdxFilesTest {

    @TempDir File tempDir;

    @Test
    void readsExistingInternalTextAndBytes() throws Exception {
        File textFile = new File(tempDir, "hello.txt");
        writeUtf8(textFile, "packaged");
        File binFile = new File(tempDir, "data.bin");
        writeBytes(binFile, new byte[] {9, 8, 7});

        GdxFiles files =
                new GdxFiles(
                        new GdxFiles.InternalOpener() {
                            @Override
                            public FileHandle open(String path) {
                                return new FileHandle(new File(tempDir, path));
                            }
                        });

        assertEquals("packaged", files.readString("hello.txt"));
        assertArrayEquals(new byte[] {9, 8, 7}, files.readBytes("data.bin"));
    }

    @Test
    void missingInternalPathThrowsEngineException() {
        GdxFiles files =
                new GdxFiles(
                        new GdxFiles.InternalOpener() {
                            @Override
                            public FileHandle open(String path) {
                                return new FileHandle(new File(tempDir, path));
                            }
                        });

        EngineException thrown =
                assertThrows(EngineException.class, () -> files.readString("gone.txt"));
        assertTrue(thrown.getMessage().contains("gone.txt"));

        thrown = assertThrows(EngineException.class, () -> files.readBytes("gone.bin"));
        assertTrue(thrown.getMessage().contains("gone.bin"));
    }

    private static void writeUtf8(File file, String text) throws Exception {
        writeBytes(file, text.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeBytes(File file, byte[] bytes) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(bytes);
        } finally {
            out.close();
        }
    }
}
