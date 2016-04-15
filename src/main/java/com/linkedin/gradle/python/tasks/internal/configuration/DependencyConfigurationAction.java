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
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;


public class DependencyConfigurationAction extends BasePythonTaskAction<InstallDependenciesTask> {

    private final PythonConfiguration configuration;
    private final String[] dependsOn;

    public DependencyConfigurationAction(PythonEnvironment pythonEnvironment,
                                         PythonConfiguration configuration,
                                         String... dependsOn) {
        super(pythonEnvironment);
        this.configuration = configuration;
        this.dependsOn = dependsOn;
    }

    @Override
    public void configure(InstallDependenciesTask task) {
        for (String taskName : dependsOn) {
            task.dependsOn(taskName);
        }
        task.setDependencyConfiguration(configuration.getConfiguration());
    }
}
