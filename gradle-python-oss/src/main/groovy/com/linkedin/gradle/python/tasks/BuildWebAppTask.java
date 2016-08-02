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

import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.util.EntryPointHelpers;
import com.linkedin.gradle.python.util.PexFileUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class BuildWebAppTask extends DefaultTask {

    private DeployableExtension deployableExtension;
    private WheelExtension wheelExtension;
    private PexExtension pexExtension;
    private File executable;
    private String entryPoint;
    private String pythonInterpreter;

    @Input
    public String getPythonInterpreter() {
        return pythonInterpreter;
    }

    @InputDirectory
    public File getPexCache() {
        return pexExtension.getPexCache();
    }

    @InputDirectory
    public File getWheelCache() {
        return wheelExtension.getWheelCache();
    }

    @OutputFile
    public File getExecutable() {
        return executable;
    }

    @OutputDirectory
    public File getBinDir() {
        return deployableExtension.getDeployableBinDir();
    }

    @Input
    public String getEntryPoint() {
        return entryPoint;
    }

    @TaskAction
    public void buildWebapp() {
        if (pexExtension.isFatPex()) {
            PexFileUtil.buildPexFile(getProject(), getPexCache(), getExecutable().getPath(), getWheelCache(), pythonInterpreter, entryPoint);
        } else {
            EntryPointHelpers.writeEntryPointScript(getProject(), getExecutable().getPath(), entryPoint);
        }
    }

    public void setDeployableExtension(DeployableExtension deployableExtension) {
        this.deployableExtension = deployableExtension;
    }

    public void setWheelExtension(WheelExtension wheelExtension) {
        this.wheelExtension = wheelExtension;
    }

    public void setPexExtension(PexExtension pexExtension) {
        this.pexExtension = pexExtension;
    }

    public void setPythonInterpreter(String pythonInterpreter) {
        this.pythonInterpreter = pythonInterpreter;
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }
}
