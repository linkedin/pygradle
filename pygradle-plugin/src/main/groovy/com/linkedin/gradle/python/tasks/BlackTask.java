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

import com.linkedin.gradle.python.extension.BlackExtension;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;


public class BlackTask extends AbstractPythonMainSourceDefaultTask {

    public void preExecution() {
        PythonDetails blackDetails = getPythonDetails();
        args(blackDetails.getVirtualEnvironment().findExecutable("black").getAbsolutePath());

        Project project = getProject();
        BlackExtension black = ExtensionUtils.getPythonComponentExtension(project, BlackExtension.class);

        String[] arguments = black.getArguments();

        if (arguments == null) {
            // Default to longer line length (160) than the default (88)
            // Default to check only
            arguments = new String[] {"--check", "-l", "160", getPythonExtension().srcDir, getPythonExtension().testDir};
        }
        args(arguments);
    }

    public void processResults(ExecResult results) {
    }
}
