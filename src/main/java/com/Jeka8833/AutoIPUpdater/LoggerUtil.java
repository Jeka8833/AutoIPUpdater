package com.Jeka8833.AutoIPUpdater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class LoggerUtil {
    private static final Logger logger = LogManager.getLogger(LoggerUtil.class);

    public static void initLogger() {
        System.setOut(createLoggingProxy(System.out, Level.INFO));
        System.setErr(createLoggingProxy(System.err, Level.ERROR));
    }

    public static void logStream(final InputStream stream, final Level level,
                                 final String prefix) {
        Main.THREAD_POOL.execute(() -> {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                buffer.lines().forEach(s -> logger.log(level, prefix + s));
            } catch (IOException e) {
                logger.error("Fail open or close logger stream", e);
            }
        });
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream, final Level level) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                logger.log(level, string);
            }
        };
    }
}
