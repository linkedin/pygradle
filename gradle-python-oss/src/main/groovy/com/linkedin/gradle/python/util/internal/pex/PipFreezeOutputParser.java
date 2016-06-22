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

        for (String it : requirements.split("\n")) {
            String[] split = it.split("==");
            String name = split[0];
            if (!ignoredDependencies.contains(name)) {
                reqs.add(name);
            }
        }

        return reqs;
    }
}
