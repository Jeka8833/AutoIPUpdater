package com.Jeka8833.AutoIPUpdater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main implements Runnable {

    private static final Logger logger = LogManager.getLogger(Main.class);
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });
    @CommandLine.Option(names = "--folder", required = true, description = "Download folder")
    public Path projectFolder;

    @CommandLine.Option(names = "--file", required = true, description = "Server list file")
    public Path serverListFile;

    @CommandLine.Option(names = "--port", required = true, description = "Websocket port")
    public int websocketPort;

    @CommandLine.Option(names = "--name", required = true, description = "Server name")
    public String serverName;

    @CommandLine.Option(names = "--commit", required = true, description = "Commit text")
    public String commitText;

    @CommandLine.Option(names = "--clone", required = true, description = "URL to git clone")
    public String gitCloneUrl;

    @CommandLine.Option(names = "--push", required = true, description = "URL to git push")
    public String gitPushUrl;

    @CommandLine.Option(names = "--zone", description = "Set time zone")
    public String timeZone = "Europe/Kiev";

    @Override
    public void run() {
        final var git = new GitManager(gitCloneUrl, gitPushUrl, projectFolder.toAbsolutePath());
        for (int i = 0; i < 3; i++) {
            try {
                boolean crash = !git.pull();
                if (!crash)
                    if (!IPManager.updateFile(serverName, serverListFile.toAbsolutePath(), websocketPort)) crash = true;
                if (!crash)
                    if (!git.push(commitText, timeZone)) crash = true;
                if (!crash) break;
            } catch (Exception e) {
                logger.error("Runnable error", e);
            }
            logger.warn("Operation is failed, the attempt will be in 10 seconds");
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                logger.warn("Thread is interrupted", e);
            }
        }
    }

    public static void main(String[] args) {
        LoggerUtil.initLogger();
        new CommandLine(new Main()).execute(args);
    }
}
