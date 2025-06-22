package io.MerahPutihTheLastStand.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.DutchSlayer.Main;

import java.io.File;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        try {
            System.out.println("Working Directory: " + System.getProperty("user.dir"));
            System.out.println("Assets folder exists: " + new File("assets").exists());
            if (StartupHelper.startNewJvmIfRequired()) return;
            createApplication();
        } catch (Exception e) {
            System.err.println("FATAL ERROR occurred:");
            e.printStackTrace();

            // Wait for user input before closing
            System.out.println("\n=== APPLICATION CRASHED ===");
            System.out.println("Check debug.log for details");
            System.out.println("Press Enter to close...");

            try {
                System.in.read();
            } catch (Exception ignore) {}
        } // This handles macOS support and helps on Windows.
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("DutchSlayer");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(1280, 720);
        configuration.setResizable(false);
        configuration.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
