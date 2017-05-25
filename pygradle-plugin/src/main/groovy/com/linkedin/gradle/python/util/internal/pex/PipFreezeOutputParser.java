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
package com.linkedin.gradle.python.util.internal.pex;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PipFreezeOutputParser {

    private PipFreezeOutputParser() {
        //private constructor for utility class
    }

    static List<String> getDependencies(Collection<String> ignoredDependencies, ByteArrayOutputStream requirements) {
        return getDependencies(ignoredDependencies, requirements.toString());
    }

    static List<String> getDependencies(Collection<String> ignoredDependencies, String requirements) {
        List<String> reqs = new ArrayList<>();

        // In regex world \n will also match the windows CR+LF
        for (String line : requirements.split("\n")) {
            String[] parts = line.split("==");
            String name = parts[0];
            boolean editable = name.startsWith("-e ");
            // The tar name can have _ when package name has -, so check both.
            if (!(editable || ignoredDependencies.contains(name)
                || ignoredDependencies.contains(name.replace("-", "_")))) {
                reqs.add(name);
            }
        }

        return reqs;
    }
}
