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
package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;
import com.linkedin.gradle.python.util.ExtensionUtils;


public class MypyExtension {
    private boolean run;
    private String[] arguments = null;
    private String srcDir;

    public MypyExtension(Project project) {
        srcDir = ExtensionUtils.getPythonExtension(project).srcDir;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public void setArguments(String argumentString) {
        arguments = argumentString.split("\\s+");
    }

    public String[] getArguments() {
        if (arguments == null) {
            return new String[]{srcDir};
        }
        return arguments;
    }
}
