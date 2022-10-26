package com.Jeka8833.AutoIPUpdater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public class GitManager {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mmZ");
    private static final Logger logger = LogManager.getLogger(GitManager.class);

    private final String url;
    private final String urlWithAuth;
    private final Path projectFolder;

    public GitManager(String url,
                      String urlWithAuth, Path projectFolder) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Git URL is null or blank");
        if (urlWithAuth == null || urlWithAuth.isBlank())
            throw new IllegalArgumentException("Git URL is null or blank");

        this.url = url;
        this.urlWithAuth = urlWithAuth;
        this.projectFolder = projectFolder;
    }

    public boolean pull() {
        try {
            deleteFolder(projectFolder);

            final Process gitProcess = Application.createProcessAndLogging(
                    "<git-pull> ", null,
                    "git", "clone", url, projectFolder.toString());
            Application.waitEnd(gitProcess);
            return true;
        } catch (Exception e) {
            logger.error("Fail pull project", e);
        }
        return false;
    }

    public boolean push(final String commitName, final String timeZone) {
        if (!Files.isDirectory(projectFolder)) return false;

        try {
            final Process addProcess = Application.createProcessAndLogging(
                    "<git-add> ", projectFolder.toFile(), "git", "add", "*");
            Application.waitEnd(addProcess);

            final Process commitProcess = Application.createProcessAndLogging(
                    "<git-commit> ", projectFolder.toFile(),
                    "git", "commit", "-m",
                    '"' + commitName.replace("[time]",
                            ZonedDateTime.now(ZoneId.of(timeZone)).format(formatter)) + '"');
            Application.waitEnd(commitProcess);

            final Process pushProcess = Application.createProcessAndLogging(
                    "<git-push> ", projectFolder.toFile(),
                    "git", "push", "--force", urlWithAuth);
            Application.waitEnd(pushProcess);

            deleteFolder(projectFolder);
            return true;
        } catch (Exception e) {
            logger.error("Fail push project", e);
        }
        return false;
    }

    private static void deleteFolder(final Path folder) {
        if (Files.isDirectory(folder)) {
            try (Stream<Path> pathStream = Files.walk(folder)) {
                //noinspection ResultOfMethodCallIgnored
                pathStream.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (Exception e) {
                logger.error("Fail delete folder", e);
            }
        }
    }
}