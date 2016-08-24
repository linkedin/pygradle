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

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.component.PythonEnvironment;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;

import java.io.File;


public class BasePythonTask extends DefaultTask {

    PythonEnvironment pythonEnvironment;

    @Input
    public PythonVersion getPythonVersion() {
        return pythonEnvironment.getVersion();
    }

    public ExecResult execute(Action<ExecAction> action) {
        return pythonEnvironment.getVirtualEnvPythonExecutable().execute(action);
    }

    public PythonEnvironment getPythonEnvironment() {
        return pythonEnvironment;
    }

    public void setPythonEnvironment(PythonEnvironment pythonEnvironment) {
        this.pythonEnvironment = pythonEnvironment;
    }

    public File getVenvDir() {
        return pythonEnvironment.getVenvDir();
    }
}
