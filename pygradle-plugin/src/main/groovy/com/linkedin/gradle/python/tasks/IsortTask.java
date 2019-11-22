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
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.extension.IsortExtension;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;


public class IsortTask extends AbstractPythonMainSourceDefaultTask {

    public void preExecution() {
        PythonDetails isortDetails = getPythonDetails();
        args(isortDetails.getVirtualEnvironment().findExecutable("isort").getAbsolutePath());

        Project project = getProject();
        IsortExtension isort = ExtensionUtils.getPythonComponentExtension(project, IsortExtension.class);

        String[] arguments = isort.getArguments();

        if (arguments == null) {
            // Default to --check-only --recursive src/ test/
            arguments = new String[]{"--check-only", "--recursive", getPythonExtension().srcDir, getPythonExtension().testDir};
        }
        args(arguments);
    }

    public void processResults(ExecResult results) {
    }
}
