/**
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.util;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileSystemUtils {

    private FileSystemUtils() {
        //private constructor for util class
    }

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
