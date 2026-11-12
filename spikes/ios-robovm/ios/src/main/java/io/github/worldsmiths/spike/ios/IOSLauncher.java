package io.github.worldsmiths.spike.ios;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

public class IOSLauncher extends IOSApplication.Delegate {
    private static int exitAfterFrames;

    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
        return new IOSApplication(new HelloGame(exitAfterFrames), configuration);
    }

    public static void main(String[] argv) {
        for (String arg : argv) {
            if ("--s3-probes".equals(arg)) {
                ProbeReport.print(System.out);
                System.exit(0);
            }
            if ("--s3-smoke".equals(arg)) {
                exitAfterFrames = 60;
            }
        }
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
