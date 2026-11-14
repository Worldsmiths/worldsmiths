package io.github.worldsmiths.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class EngineInfo {
    private EngineInfo() {}

    public static String group() {
        InputStream in = EngineInfo.class.getResourceAsStream("/worldsmiths-group.txt");
        if (in == null) {
            throw new IllegalStateException("missing group id resource");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[128];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            in.close();
            return new String(out.toByteArray(), "UTF-8").trim();
        } catch (IOException e) {
            throw new IllegalStateException("unreadable group id resource", e);
        }
    }
}
