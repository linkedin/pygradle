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
package com.linkedin.gradle.python.plugin.testutils;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

public class PythonTemporaryFolder extends TemporaryFolder {

    public PythonTemporaryFolder(File parentFolder) {
        super(parentFolder);
    }

    @Override
    public void delete() {
        if (getRoot() != null) {
            deleteRecursiveIfExists(getRoot());
        }
    }

    public static boolean deleteRecursiveIfExists(File item) {
        if (!item.exists()) {
            return true;
        }
        if (!Files.isSymbolicLink(item.toPath()) && item.isDirectory()) {
            File[] subitems = item.listFiles();
            assert subitems != null;
            for (File subitem : subitems) {
                if (!deleteRecursiveIfExists(subitem)) {
                    return false;
                }
            }
        }
        return item.delete();
    }
}
