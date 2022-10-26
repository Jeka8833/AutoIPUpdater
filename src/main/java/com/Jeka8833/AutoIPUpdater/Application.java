package com.Jeka8833.AutoIPUpdater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Application {
    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void waitEnd(final Process process) {
        while (true) {
            try {
                process.waitFor();
                return;
            } catch (InterruptedException e) {
                logger.warn("An exception occurred while waiting for the process to finish.", e);
            }
        }
    }

    public static Process createProcessAndLogging(final String consolePrefix, final File workDirectory,
                                                  final String... command) throws IOException {
        final Process process = createProcess(workDirectory, command);

        LoggerUtil.logStream(process.getInputStream(), Level.INFO, consolePrefix);
        LoggerUtil.logStream(process.getErrorStream(), Level.ERROR, consolePrefix);
        return process;
    }

    public static Process createProcess(final File workDirectory, final String... command) throws IOException {
        final String[] outArgs = switch (Util.getPlatform()) {
            case WINDOWS -> new String[]{"cmd.exe", "/C", String.join(" ", command)};
            case LINUX -> new String[]{"/bin/bash", "-c", String.join(" ", command)};
            default -> command;
        };

        final var builder = new ProcessBuilder(outArgs);
        builder.directory(workDirectory);
        //noinspection BlockingMethodInNonBlockingContext
        return builder.start();
    }
}
