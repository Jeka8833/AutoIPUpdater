package com.Jeka8833.AutoIPUpdater;

import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPManager {

    private static final Gson GSON = new Gson();
    private static final Logger logger = LogManager.getLogger(IPManager.class);

    private static final Pattern ipv4Pattern = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");

    public static boolean updateFile(final String serverName, final Path editedFile, final int webSocketPort) {
        final String ip = getIP();
        if (ip == null || ip.isBlank()) return false;
        logger.info("Current IP: " + ip);

        final var currentServer = new Server(serverName, "ws://" + ip +
                (webSocketPort <= 0 ? "" : ":" + webSocketPort), ip);

        ServerManager serverManager = readFile(editedFile);
        if (serverManager == null) serverManager = new ServerManager(new HashSet<>());


        if (serverManager.serverLists == null) {
            serverManager.serverLists = new HashSet<>();
        } else {
            serverManager.serverLists.remove(currentServer);
        }

        serverManager.serverLists.add(currentServer);

        return writeFile(editedFile, serverManager);
    }

    private static String getIP() {
        try {
            Process process = Application.createProcess(null, "ec2-metadata", "-v");
            LoggerUtil.logStream(process.getErrorStream(), Level.ERROR, "<ec2-metadata> ");

            var readAnswer = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Application.waitEnd(process);   // May not be needed xD

            // Answer example: Server IP public-ipv4: 3.109.221.250
            final Matcher m = ipv4Pattern.matcher(readAnswer);
            if (!m.find()) return null;

            return m.group();
        } catch (Exception e) {
            logger.error("Fail get or process IPv4", e);
        }
        return null;
    }

    private static ServerManager readFile(final Path file) {
        if (file == null) return null;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, ServerManager.class);
        } catch (Exception e) {
            logger.warn("Fail read file", e);
        }
        return null;
    }

    private static boolean writeFile(final Path path, final ServerManager serverManager) {
        try {
            Files.writeString(path, GSON.toJson(serverManager), StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            logger.error("Fail write file", e);
        }
        return false;
    }

    private static class ServerManager {
        private HashSet<Server> serverLists;

        private ServerManager(HashSet<Server> serverLists) {
            this.serverLists = serverLists;
        }
    }

    private static class Server {
        private String name;
        private String ip;
        private String clearIP;

        private Server(String name, String ip, String clearIP) {
            this.name = name;
            this.ip = ip;
            this.clearIP = clearIP;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Server server)) return false;

            return name.equals(server.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
