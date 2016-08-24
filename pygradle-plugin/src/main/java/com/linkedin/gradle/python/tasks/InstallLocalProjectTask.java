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

import com.linkedin.gradle.python.tasks.internal.utilities.PipLocalInstallAction;
import com.linkedin.gradle.python.tasks.internal.utilities.TaskUtils;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;


public class InstallLocalProjectTask extends BasePythonTask {

    @TaskAction
    public void installLocalProject() {
        PipLocalInstallAction pipLocalInstallAction = new PipLocalInstallAction(getVenvDir());
        ExecResult execute = execute(pipLocalInstallAction.install(getProject().getProjectDir()));
        if (execute.getExitValue() != 0) {
            getLogger().lifecycle(pipLocalInstallAction.getWholeText());
        }
        execute.assertNormalExitValue();
    }

    @OutputFile
    public File getEggLink() {
        String fileName = String.format("%s.egg-link", getProject().getName());
        File installLink = new File(TaskUtils.sitePackage(getVenvDir(), getPythonVersion()), fileName);
        getLogger().info("Link: {}", installLink.getAbsolutePath());
        return installLink;
    }

    @InputFile
    public File getSetupPyFile() {
        File file = new File(getProject().getProjectDir(), "setup.py");
        getLogger().info("Setup.py: {}", file.getAbsolutePath());
        return file;
    }

}
