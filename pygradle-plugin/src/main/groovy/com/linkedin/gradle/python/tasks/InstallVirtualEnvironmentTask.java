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

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.action.CreateVirtualEnvAction;
import com.linkedin.gradle.python.tasks.action.VirtualEnvCustomizer;
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import com.linkedin.gradle.python.tasks.provides.ProvidesVenv;
import com.linkedin.gradle.python.tasks.supports.SupportsDistutilsCfg;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class InstallVirtualEnvironmentTask extends DefaultTask implements FailureReasonProvider, SupportsDistutilsCfg,
    ProvidesVenv {

    private PythonDetails pythonDetails;
    private String distutilsCfg;
    private final TeeOutputContainer container = new TeeOutputContainer();
    private EditablePythonAbiContainer editablePythonAbiContainer;

    @InputFiles
    public Configuration getPyGradleBootstrap() {
        return getProject().getConfigurations().getByName("pygradleBootstrap");
    }

    @OutputFile
    public File getVirtualEnvInterpreter() {
        return pythonDetails.getVirtualEnvInterpreter();
    }

    @TaskAction
    public void installVEnv() {
        CreateVirtualEnvAction action = new CreateVirtualEnvAction(getProject(), pythonDetails, editablePythonAbiContainer);
        action.buildVenv(new VirtualEnvCustomizer(distutilsCfg, new ProjectExternalExec(getProject()), pythonDetails));
    }

    @Override
    public String getReason() {
        return container.getCommandOutput();
    }

    public void setPythonDetails(PythonDetails pythonDetails) {
        this.pythonDetails = pythonDetails;
    }

    public PythonDetails getPythonDetails() {
        return pythonDetails;
    }

    @Input
    @Optional
    public String getDistutilsCfg() {
        return distutilsCfg;
    }

    public void setDistutilsCfg(String distutilsCfg) {
        this.distutilsCfg = distutilsCfg;
    }

    @Override
    public void setEditablePythonAbiContainer(EditablePythonAbiContainer editablePythonAbiContainer) {
        this.editablePythonAbiContainer = editablePythonAbiContainer;
    }
}
