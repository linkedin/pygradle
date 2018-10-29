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

import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.io.File;
import java.util.LinkedHashMap;

public class PythonWheelDistributionPlugin extends PythonBasePlugin {
    @Override
    public void applyTo(final Project project) {
        // XXX: This needs to be adjusted to work with a build matrix one day. Until
        // that is ready, we always assume pure Python 2.7 on Linux.
        String version = project.getVersion().toString().replace("-", "_");
        String name = project.getName().replace("-", "_");
        final File wheelArtifact = new File(project.getProjectDir(), "/dist/" + name + "-" + version + "-py2-none-any.whl");

        /*
         * Create a Python wheel distribution.
         */
        project.getTasks().create(TASK_PACKAGE_WHEEL, task -> {
            task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue()));
            task.getOutputs().file(wheelArtifact);
            task.doLast(it -> project.exec(execSpec -> {
                execSpec.environment(settings.pythonEnvironmentDistgradle);
                execSpec.commandLine(settings.getDetails().getVirtualEnvInterpreter(), "setup.py", "bdist_wheel");
            }));
        });

        LinkedHashMap<String, Object> wheelArtifactInfo = new LinkedHashMap<>(5);
        wheelArtifactInfo.put("name", name);
        wheelArtifactInfo.put("classifier", "py2-none-any");
        wheelArtifactInfo.put("type", "whl");
        wheelArtifactInfo.put("file", wheelArtifact);
        wheelArtifactInfo.put("builtBy", project.getTasks().getByName(TASK_PACKAGE_WHEEL));

        if (!version.contains("SNAPSHOT")) {
            project.getArtifacts().add(StandardTextValues.CONFIGURATION_WHEEL.getValue(), wheelArtifactInfo);
        }
    }

    public static final String TASK_PACKAGE_WHEEL = "packageWheel";
}
