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

package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.plugin.extension.PythonConfiguration;
import com.linkedin.gradle.python.spec.component.PythonEnvironment;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;


public class CreateVirtualEnvConfigureAction extends BasePythonTaskAction<VirtualEnvironmentBuild> {
    private final PythonConfiguration bootstrapConfiguration;

    public CreateVirtualEnvConfigureAction(final PythonEnvironment pythonEnvironment,
                                           final PythonConfiguration bootstrapConfiguration) {
        super(pythonEnvironment);
        this.bootstrapConfiguration = bootstrapConfiguration;
    }

    @Override
    public void configure(VirtualEnvironmentBuild task) {
        task.setVirtualEnvFiles(bootstrapConfiguration.getConfiguration());

        String environmentName = getPythonEnvironment().getEnvironmentName();
        String versionString = getPythonEnvironment().getVersion().getVersionString();

        task.setActivateScriptName(String.format("activate-%s-%s", environmentName, versionString));
        task.setVirtualEnvName(String.format("(%s-%s)", environmentName, versionString));
    }
}
