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

import org.gradle.api.Project;
import org.gradle.api.tasks.OutputFile;
import org.gradle.process.ExecResult;

import java.io.File;


public class SourceDistTask extends AbstractPythonMainSourceDefaultTask {

    public SourceDistTask() {
        args("setup.py", "sdist", "--dist-dir", getDistDir().getAbsolutePath());
    }

    @Override
    public void processResults(ExecResult execResult) {
    }

    @OutputFile
    public File getSdistOutput() {
        Project project = getProject();
        return new File(
            getDistDir(),
            String.format(
                "%s-%s.tar.gz",
                project.getName(),
                // TODO: Is this replace here really necessary?
                project.getVersion().toString().replace("_", "-")));
    }

    private File getDistDir() {
        return new File(getProject().getBuildDir(), "distributions");
    }

}
