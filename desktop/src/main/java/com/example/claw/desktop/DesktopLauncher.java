package com.example.claw.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.example.claw.ClawGame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DesktopLauncher {
    public static void main(String[] args) {
        if (isMacOs() && !isStartedOnFirstThread()) {
            relaunchOnFirstThread(args);
            return;
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Captain Claw Inspired Platformer");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);

        new Lwjgl3Application(new ClawGame(), config);
    }

    private static boolean isMacOs() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase().contains("mac");
    }

    private static boolean isStartedOnFirstThread() {
        String javaStartedOnFirstThread = System.getenv("JAVA_STARTED_ON_FIRST_THREAD");
        return "1".equals(javaStartedOnFirstThread);
    }

    private static void relaunchOnFirstThread(String[] args) {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + "/bin/java");
        command.add("-XstartOnFirstThread");
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(DesktopLauncher.class.getName());
        for (String arg : args) {
            command.add(arg);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Failed to relaunch with -XstartOnFirstThread. Exit code: " + exitCode);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not relaunch JVM with -XstartOnFirstThread.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for relaunched JVM.", e);
        }
    }
}
