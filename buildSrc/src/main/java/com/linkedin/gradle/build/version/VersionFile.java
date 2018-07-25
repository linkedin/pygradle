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
package com.linkedin.gradle.build.version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

class VersionFile {
    private static final String VERSION = "version";

    static Version getVersion(File propertyFile) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(propertyFile));
            return new Version(properties.getProperty(VERSION));
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

}
