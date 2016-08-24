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

import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;

import java.io.File;
import java.util.Collections;
import java.util.List;


public class BuildWheelTask extends AbstractDistTask {
    public BuildWheelTask() {
        super("bdist_wheel");
    }

    public List<String> extraArgs() {
        return Collections.singletonList(String.format("--python-tag=py%s", getPythonVersion().getVersionString().replace(".", "")));
    }

    @Override
    public String getExtension() {
        return "whl";
    }

    @Override
    //TODO: This is bad, but since pip can't tell us the name of the artifact before it's built, it must be done :-(
    protected File getPythonArtifact() {
        return new File(distributablePath,
                String.format("%s-%s-py%s-%s.%s", getProject().getName(), getProject().getVersion(),
                        getPythonEnvironment().getVersion().getMajorMinorVersion().replace(".", ""),
                        System.getProperty("os.name"),
                        getExtension()));
    }

}
