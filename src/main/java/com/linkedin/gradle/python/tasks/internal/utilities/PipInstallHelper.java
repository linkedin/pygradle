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

package com.linkedin.gradle.python.tasks.internal.utilities;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.PipInstallExecAction;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;


public class PipInstallHelper {

    private static final Logger logger = Logging.getLogger(PipInstallHelper.class);

    private final PythonExecutable pythonExecutable;
    private final PipDependencyInstallAction action;

    public PipInstallHelper(PythonExecutable pythonExecutable, PipDependencyInstallAction action) {
        this.pythonExecutable = pythonExecutable;
        this.action = action;
    }

    public void install(File dependency) {
        execute(action.install(dependency));
    }

    public void forceInstall(File dependency) {
        execute(action.forceInstall(dependency));
    }

    public void execute(PipInstallExecAction install) {
        ExecResult execute = pythonExecutable.execute(install);
        if (execute.getExitValue() != 0) {
            logger.lifecycle(action.getWholeText());
            execute.assertNormalExitValue();
        }
    }

    public void uninstall(File file) {
        //todo(ethall)
        // Need to find a way to get the package name from the file name.
    }
}
