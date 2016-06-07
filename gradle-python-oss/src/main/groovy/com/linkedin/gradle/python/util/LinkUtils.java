package com.linkedin.gradle.python.util;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LinkUtils {

    /**
     * Make a link
     * <p>
     * Make a link using the system's ``ln`` command.
     * <p>
     * @param project The project to run within.
     * @param target The target directory that the link points to.
     * @param destination The destination directory or the name of the link.
     * @param symlink Whether to create a link or symlink.
     */
    public static void makeLink(Project project, File target, File destination, boolean symlink) throws IOException {
        /*
         * Check if the file exists because the link checking logic in Gradle differs
         * between Linux and OS X machines.
         */
        if (!project.file(destination).exists()) {

            if (symlink) {
                Files.createSymbolicLink(destination.toPath(), target.toPath());
            } else {
                Files.createLink(destination.toPath(), target.toPath());
            }
        }
    }
}
