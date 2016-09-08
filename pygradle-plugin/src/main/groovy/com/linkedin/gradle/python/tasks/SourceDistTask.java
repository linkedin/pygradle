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

import java.io.File;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;


public class SourceDistTask extends DefaultTask implements FailureReasonProvider {

    private TeeOutputContainer container = new TeeOutputContainer();
    @TaskAction
    public void packageSdist() {

        final PythonExtension settings = getProject().getExtensions().getByType(PythonExtension.class);

        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                container.execute(execSpec);
                execSpec.environment(settings.pythonEnvironmentDistgradle);
                execSpec.commandLine(
                        VirtualEnvExecutableHelper.getPythonInterpreter(settings.getDetails()),
                        "setup.py",
                        "sdist",
                        "--dist-dir",
                        getDistDir().getAbsolutePath());
            }
        });
    }

    @OutputFile
    public File getSdistOutput() {
        Project project = getProject();
        return new File(getDistDir(), String.format("%s-%s.tar.gz", project.getName(), project.getVersion()));
    }

    private File getDistDir() {
        return new File(getProject().getBuildDir(), "distributions");
    }

    @Override
    public String getReason() {
        return container.getCommandOutput();
    }
}
