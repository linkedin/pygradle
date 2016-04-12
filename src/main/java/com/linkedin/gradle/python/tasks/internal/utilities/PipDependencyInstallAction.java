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

import com.linkedin.gradle.python.tasks.internal.PipInstallExecAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PipDependencyInstallAction {

    final File venvDir;
    final PipOutputStreamProcessor pipProcessor = new PipOutputStreamProcessor();

    public PipDependencyInstallAction(File venvDir) {
        this.venvDir = venvDir;
    }

    public PipInstallExecAction install(File dependency) {
        return doInstall(Arrays.asList("--no-deps", dependency.getAbsolutePath()));
    }

    public PipInstallExecAction forceInstall(File dependency) {
        return doInstall(Arrays.asList("--no-deps", "--force-reinstall", dependency.getAbsolutePath()));
    }

    private PipInstallExecAction doInstall(List<String> arguments) {
        return new PipInstallExecAction(venvDir, pipProcessor, arguments);
    }

    public String getWholeText() {
        return pipProcessor.getWholeText();
    }

    public Set<String> getPackages() {
        return pipProcessor.getPackages();
    }
}
