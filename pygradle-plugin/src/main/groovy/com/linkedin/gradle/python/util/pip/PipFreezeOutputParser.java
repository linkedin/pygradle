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
package com.linkedin.gradle.python.util.pip;

import org.gradle.api.GradleException;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PipFreezeOutputParser {

    private PipFreezeOutputParser() {
        //private constructor for utility class
    }

    static Map<String, String> getDependencies(Collection<String> ignoredDependencies, ByteArrayOutputStream requirements) {
        return getDependencies(ignoredDependencies, requirements.toString());
    }

    static Map<String, String> getDependencies(Collection<String> ignoredDependencies, String requirements) {
        Map<String, String> reqs = new HashMap<>();

        // In regex world \n will also match the windows CR+LF
        for (String line : requirements.split("\n")) {
            if (!line.startsWith("-e ")) {  // ignore editable requirements
                String[] parts = line.split("==");
                if (parts.length != 2) {
                    throw new GradleException("Unsupported requirement format. expected: <requirement>==<version>. found: " + line);
                }
                String name = parts[0];
                String version = parts[1];
                /*
                 * The tar name can have _ when package name has -, so check both.
                 * The version will convert - into _ for wheel builds, so convert right here.
                 */
                if (!(ignoredDependencies.contains(name) || ignoredDependencies.contains(name.replace("-", "_")))) {
                    reqs.put(name, version);
                }
            }
        }

        return reqs;
    }
}
