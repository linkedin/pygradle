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

import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.tasks.LayeredWheelCacheTask;
import com.linkedin.gradle.python.tasks.provides.ProvidesVenv;
import com.linkedin.gradle.python.tasks.supports.SupportsWheelCache;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import com.linkedin.gradle.python.wheel.LayeredWheelCache;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import static com.linkedin.gradle.python.util.StandardTextValues.TASK_FLAKE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_VENV_CREATE;


/**
 * The plugin provides layered wheel cache to builds.
 */
public class LayeredWheelCachePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(project);
        EditablePythonAbiContainer supportedWheelFormats = ExtensionUtils.getEditablePythonAbiContainer(project);
        LayeredWheelCache wheelCache = new LayeredWheelCache(wheelExtension.getLayeredCacheMap(), supportedWheelFormats);

        TaskContainer tasks = project.getTasks();

        tasks.withType(ProvidesVenv.class, task -> task.setEditablePythonAbiContainer(supportedWheelFormats));

        tasks.withType(SupportsWheelCache.class, task -> task.setWheelCache(wheelCache));

        tasks.create(LayeredWheelCacheTask.TASK_LAYERED_WHEEL_CACHE, LayeredWheelCacheTask.class, task -> {
            tasks.getByName(TASK_VENV_CREATE.getValue()).dependsOn(task);
            tasks.getByName(TASK_FLAKE.getValue()).dependsOn(task);
        });
    }
}
