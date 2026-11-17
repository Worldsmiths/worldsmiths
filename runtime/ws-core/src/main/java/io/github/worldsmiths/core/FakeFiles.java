package io.github.worldsmiths.core;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory {@link Files} for headless tests. Authors {@link #putInternal} packaged content, then
 * exercise {@link #readString} / {@link #readBytes} without a display or asset classpath.
 */
public final class FakeFiles implements Files {
    private final Map<String, byte[]> packaged = new HashMap<String, byte[]>();

    /** Stores UTF-8 text at an internal path for later reads. */
    public void putInternal(String path, String text) {
        packaged.put(path, text.getBytes(StandardCharsets.UTF_8));
    }

    /** Stores raw bytes at an internal path for later reads. */
    public void putInternal(String path, byte[] bytes) {
        packaged.put(path, copy(bytes));
    }

    @Override
    public String readString(String path) {
        return new String(requireBytes(path), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readBytes(String path) {
        return copy(requireBytes(path));
    }

    private byte[] requireBytes(String path) {
        byte[] data = packaged.get(path);
        if (data == null) {
            throw new EngineException("Missing internal file: " + path);
        }
        return data;
    }

    private static byte[] copy(byte[] bytes) {
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }
}
