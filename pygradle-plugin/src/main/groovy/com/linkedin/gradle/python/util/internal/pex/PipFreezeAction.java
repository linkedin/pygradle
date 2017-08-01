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
package com.linkedin.gradle.python.util.internal.pex;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecSpec;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.StandardTextValues;

class PipFreezeAction {

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

        if (Objects.equals(settings.getDetails().getPythonVersion().getPythonMajorMinor(), "2.6")
            && developmentDependencies.contains("argparse")) {
            developmentDependencies.remove("argparse");
        }

        final ByteArrayOutputStream requirements = new ByteArrayOutputStream();

        project.exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.environment(settings.getEnvironment());
                execSpec.commandLine(
                    settings.getDetails().getVirtualEnvInterpreter(),
                    settings.getDetails().getVirtualEnvironment().getPip(),
                    "freeze",
                    "--disable-pip-version-check"
                );
                execSpec.setStandardOutput(requirements);
            }
        });

        Map<String, String> dependencies = PipFreezeOutputParser.getDependencies(developmentDependencies, requirements);
        // for snapshot builds, wheel gets built with _SNAPSHOT(where as it's -SNAPSHOT in gradle land), and the version becomes <semver>._SNAPSHOT
        dependencies.put(project.getName(), project.getVersion().toString().replace("-SNAPSHOT", "_SNAPSHOT"));
        return dependencies;
    }

    private static Set<String> configurationToSet(Project project, String configurationName) {
        return configurationToSet(project.getConfigurations().getByName(configurationName).getFiles());
    }

    private static Set<String> configurationToSet(Collection<File> files) {
        Set<String> configNames = new HashSet<>();
        for (File file : files) {
            PackageInfo packageInfo = PackageInfo.fromPath(file.getName());
            configNames.add(packageInfo.getName());
        }

        return configNames;
    }
}
