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
package com.linkedin.gradle.python.util.pip;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PipFreezeAction {
    private final Project project;

    public PipFreezeAction(Project project) {
        this.project = project;
    }

    public Map<String, String> getDependencies() {
        final PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        // Setup requirements, build, and test dependencies
        Set<String> developmentDependencies = configurationToSet(project, StandardTextValues.CONFIGURATION_SETUP_REQS.getValue());
        developmentDependencies.addAll(configurationToSet(project, StandardTextValues.CONFIGURATION_BUILD_REQS.getValue()));
        developmentDependencies.addAll(configurationToSet(project, StandardTextValues.CONFIGURATION_TEST.getValue()));

        developmentDependencies.removeAll(configurationToSet(project, StandardTextValues.CONFIGURATION_PYTHON.getValue()));

        final ByteArrayOutputStream requirements = new ByteArrayOutputStream();

        /*
         * NOTE: It is very important to provide "--all" in the list of arguments
         * to "pip freeze". Otherwise, setuptools, wheel, or pip would not be included
         * even if required by runtime configuration "python".
         */
        project.exec(execSpec -> {
            execSpec.environment(settings.getEnvironment());
            execSpec.commandLine(
                settings.getDetails().getVirtualEnvInterpreter(),
                settings.getDetails().getVirtualEnvironment().getPip(),
                "freeze",
                "--all",
                "--disable-pip-version-check"
            );
            execSpec.setStandardOutput(requirements);
        });

        Map<String, String> dependencies = PipFreezeOutputParser.getDependencies(developmentDependencies, requirements);
        // Always add project unconditionally.
        dependencies.put(project.getName(), project.getVersion().toString());
        return dependencies;
    }

    private static Set<String> configurationToSet(Project project, String configurationName) {
        return configurationToSet(project.getConfigurations().getByName(configurationName).getFiles());
    }

    private static Set<String> configurationToSet(Collection<File> files) {
        Set<String> configNames = new HashSet<>();
        for (File file : files) {
            PackageInfo packageInfo = PackageInfo.fromPath(file);
            configNames.add(packageInfo.getName());
        }

        return configNames;
    }
}
