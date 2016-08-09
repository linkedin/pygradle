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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.VirtualEnvExecutableHelper;
import com.linkedin.gradle.python.util.internal.pex.FatPexGenerator;
import com.linkedin.gradle.python.util.internal.pex.ThinPexGenerator;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import java.util.Map;

public class BuildPexTask extends DefaultTask {

    @Input
    @Optional
    public Map<String, String> additionalProperties;

    @Input
    @Optional
    public String entryPointTemplate;


    @TaskAction
    public void buildPex() throws Exception {
        Project project = getProject();

        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension.class);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);

        // Recreate the pex cache if it exists so that we don't mistakenly use an old build's version of the local project.
        if (pexExtension.getPexCache().exists()) {
            FileUtils.deleteQuietly(pexExtension.getPexCache());
            pexExtension.getPexCache().mkdirs();
        }

        project.exec(execSpec -> configureExecution(pythonExtension, execSpec));
        deployableExtension.getDeployableBuildDir().mkdirs();

        if (pexExtension.isFatPex()) {
            new FatPexGenerator(project).buildEntryPoints();
        } else {
            new ThinPexGenerator(project, entryPointTemplate, additionalProperties).buildEntryPoints();
        }
    }

    public void configureExecution(PythonExtension pythonExtension, ExecSpec spec) {
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(getProject());

        spec.environment(pythonExtension.pythonEnvironment);
        spec.environment(pythonExtension.pythonEnvironmentDistgradle);
        spec.commandLine(VirtualEnvExecutableHelper.getPythonInterpreter(pythonExtension));
        spec.args(VirtualEnvExecutableHelper.getPip(pythonExtension),
            "wheel",
            "--disable-pip-version-check",
            "--wheel-dir",
            wheelExtension.getWheelCache().getAbsolutePath(),
            "--no-deps",
            ".");
    }

}
