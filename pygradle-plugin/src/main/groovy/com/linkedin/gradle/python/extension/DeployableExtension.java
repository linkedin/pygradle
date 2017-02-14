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
package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;

import java.io.File;


public class DeployableExtension {

    private File deployableBuildDir;
    private File deployableBinDir;
    private File deployableEtcDir;

    public DeployableExtension(Project project) {
        deployableBuildDir = new File(project.getBuildDir(), "deployable");
        deployableBinDir = new File(deployableBuildDir, "bin");
        deployableEtcDir = new File(deployableBuildDir, "etc");
    }

    public File getDeployableBuildDir() {
        return deployableBuildDir;
    }

    public void setDeployableBuildDir(File deployableBuildDir) {
        this.deployableBuildDir = deployableBuildDir;
    }

    public File getDeployableBinDir() {
        return deployableBinDir;
    }

    public void setDeployableBinDir(File deployableBinDir) {
        this.deployableBinDir = deployableBinDir;
    }

    public File getDeployableEtcDir() {
        return deployableEtcDir;
    }

    public void setDeployableEtcDir(File deployableEtcDir) {
        this.deployableEtcDir = deployableEtcDir;
    }
}
