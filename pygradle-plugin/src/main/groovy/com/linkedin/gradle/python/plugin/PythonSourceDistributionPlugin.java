/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.tasks.SourceDistTask;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.util.LinkedHashMap;

public class PythonSourceDistributionPlugin extends PythonBasePlugin {

    public static final String TASK_PACKAGE_SDIST = "packageSdist";

    /**
     * Create a Python source distribution.
     */
    @Override
    public void applyTo(final Project project) {

        SourceDistTask sdistPackageTask = project.getTasks().create(TASK_PACKAGE_SDIST, SourceDistTask.class,
            task -> task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue())));

        LinkedHashMap<String, Object> sdistArtifactInfo = new LinkedHashMap<>(5);
        sdistArtifactInfo.put("name", project.getName());
        sdistArtifactInfo.put("type", "tgz");
        sdistArtifactInfo.put("extension", "tar.gz");
        sdistArtifactInfo.put("file", sdistPackageTask.getSdistOutput());
        sdistArtifactInfo.put("builtBy", project.getTasks().getByName(TASK_PACKAGE_SDIST));

        project.getArtifacts().add(StandardTextValues.CONFIGURATION_DEFAULT.getValue(), sdistArtifactInfo);
    }
}
