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
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

import static com.linkedin.gradle.python.util.values.PyGradleTask.*;

public class PythonCliDistributionPlugin extends AbstractPluginBase {

    @Override
    public void applyTo(Project project) {
        addPluginLocal(PythonPexDistributionPlugin.class);

        ExtensionUtils.maybeCreateCliExtension(project);

        addTaskLocal(buildGenerateCompletionsTask());

        aDependsOnB(GENERATE_COMPLETIONS, INSTALL_PROJECT);
        aDependsOnB(BUILD_PEX, GENERATE_COMPLETIONS);
    }

    private Map<String, Object> buildGenerateCompletionsTask() {
        Map<String, Object> myTask = new HashMap<>();

        myTask.put("name", GENERATE_COMPLETIONS);
        myTask.put("type", GenerateCompletionsTask.class);

        return myTask;
    }
}
