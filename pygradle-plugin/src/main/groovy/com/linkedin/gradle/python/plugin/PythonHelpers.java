/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.plugin;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ConsoleOutput;

public class PythonHelpers {

    public static final int LINE_WIDTH = 80;

    private PythonHelpers() {
        // noop
    }

    public static boolean isPlainOrVerbose(Project project) {
        return project.getLogger().isInfoEnabled() || System.getenv("TERM") == null
            || project.getGradle().getStartParameter().getConsoleOutput() == ConsoleOutput.Plain;
    }

    public static String createPrettyLine(String prefix, String postfix) {
        StringBuilder successFlair = new StringBuilder();


        successFlair.append(prefix).append(" ");
        for (int i = 0; i < LINE_WIDTH - 3 - prefix.length() - postfix.length(); i++) {
            successFlair.append(".");
        }

        successFlair.append(".");
        successFlair.append(" ").append(postfix);

        return successFlair.toString();

    }
}
