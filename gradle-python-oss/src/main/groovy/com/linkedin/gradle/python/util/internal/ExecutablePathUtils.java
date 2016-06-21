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
package com.linkedin.gradle.python.util.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ExecutablePathUtils {

    private ExecutablePathUtils() {
        //private constructor for util class
    }

    public static File getExecutable(List<File> pathList, String exeName) {
        for (File dir : pathList) {
            File candidate = new File(dir, exeName);
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }

    public static List<File> getPath() {
        List<File> entries = new ArrayList<>();

        String path = System.getenv("PATH");
        if (path == null) {
            return entries;
        }

        for (String entry : path.split(Pattern.quote(File.pathSeparator))) {
            entries.add(new File(entry));
        }
        return entries;
    }

}
