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

import com.linkedin.gradle.python.tasks.FindAbiForCurrentPythonTask;
import com.linkedin.gradle.python.tasks.ParallelWheelGenerationTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.wheel.CachedBackedWheelCache;
import com.linkedin.gradle.python.wheel.SupportedWheelFormats;
import com.linkedin.gradle.python.wheel.SupportsWheelCache;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;
import java.util.Objects;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYDOCS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYTHON;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_SETUP_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_TEST;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_VENV;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_WHEEL;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_SETUP_REQS;

public class WheelFirstPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();

        File cacheDir = new File(project.getGradle().getGradleUserHomeDir(), "pygradle-wheel-cache");

        project.getPlugins().withType(PythonPlugin.class, plugin -> {

            SupportedWheelFormats supportedWheelFormats = new SupportedWheelFormats();
            CachedBackedWheelCache wheelCache = new CachedBackedWheelCache(cacheDir, supportedWheelFormats);

            FindAbiForCurrentPythonTask abiScript = tasks.create("findPythonAbi", FindAbiForCurrentPythonTask.class, it -> {
                it.dependsOn(tasks.getByName(TASK_INSTALL_SETUP_REQS.getValue()));
                it.setSupportedWheelFormat(supportedWheelFormats);
                it.addPythonDetails(() -> ExtensionUtils.getPythonExtension(project).getDetails());
            });

            ParallelWheelGenerationTask parallelWheelTask = tasks.create("parallelWheels", ParallelWheelGenerationTask.class, it -> {
                it.dependsOn(abiScript);
                ConfigurationContainer configurations = project.getConfigurations();
                FileCollection dependencies = configurations.getByName(CONFIGURATION_SETUP_REQS.getValue())
                    .plus(configurations.getByName(CONFIGURATION_BUILD_REQS.getValue()))
                    .plus(configurations.getByName(CONFIGURATION_PYTHON.getValue()))
                    .plus(configurations.getByName(CONFIGURATION_WHEEL.getValue()))
                    .plus(configurations.getByName(CONFIGURATION_PYDOCS.getValue()))
                    .plus(configurations.getByName(CONFIGURATION_VENV.getValue()))
                    .plus(configurations.getByName(CONFIGURATION_TEST.getValue()));

                it.setFilesToConvert(dependencies);
                it.setWheelCache(wheelCache);
                it.setCacheDir(cacheDir);
                it.dependsOn(tasks.getByName(TASK_INSTALL_SETUP_REQS.getValue()));
            });

            tasks.withType(SupportsWheelCache.class, it -> {
                it.setWheelCache(wheelCache);

                if (!Objects.equals(it.getName(), TASK_INSTALL_SETUP_REQS.getValue())) {
                    it.dependsOn(parallelWheelTask);
                }
            });
        });
    }
}
