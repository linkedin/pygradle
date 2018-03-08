/*
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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

public class FileSystemUtils {

    private FileSystemUtils() {
        //private constructor for util class
    }

    /**
     * Make a link using the system's ``ln`` command.
     *
     * @param target      The target directory that the link points to.
     * @param destination The destination directory or the name of the link.
     * @throws IOException if symlink can't be made
     */
    public static void makeSymLink(File target, File destination) throws IOException {
        /*
         * Check if the file exists because the link checking logic in Gradle differs
         * between Linux and OS X machines.
         */
        if (OperatingSystem.current().isUnix()) {
            if (!Files.exists(destination.toPath())) {
                Files.createSymbolicLink(destination.toPath(), target.toPath());
            }
        } else {
            if (!Files.exists(destination.toPath())) {
                Files.copy(target.toPath(), destination.toPath());
            }
        }
    }

    public static void makeSymLinkUnchecked(File target, File destination) {
        try {
            makeSymLink(target, destination);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
