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
package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.tasks.GenerateCompletionsTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValuesTasks;
import org.gradle.api.Project;
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.GENERATE_COMPLETIONS;
import static com.linkedin.gradle.python.util.StandardTextValuesTasks.BUILD_PEX;

public class PythonCliDistributionPlugin extends PythonBasePlugin {

    @Override
    public void applyTo(Project project) {
        project.getPlugins().apply(PythonPexDistributionPlugin.class);
        ExtensionUtils.maybeCreateCliExtension(project);

        GenerateCompletionsTask completionsTask = project.getTasks().create(GENERATE_COMPLETIONS.getValue(), GenerateCompletionsTask.class);
        completionsTask.dependsOn(project.getTasks().getByName(StandardTextValuesTasks.INSTALL_PROJECT.getValue()));

        project.getTasks().getByName(BUILD_PEX.getValue()).dependsOn(project.getTasks().getByName(GENERATE_COMPLETIONS.getValue()));
    }

}
