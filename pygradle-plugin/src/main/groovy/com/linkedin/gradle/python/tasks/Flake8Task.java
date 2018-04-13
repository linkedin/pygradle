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
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.action.CreateVirtualEnvAction;
import com.linkedin.gradle.python.tasks.action.PipInstallAction;
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings;
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.EmptyWheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Flake8Task extends AbstractPythonInfrastructureDefaultTask implements SupportsPackageInfoSettings {

    private static final Logger log = Logging.getLogger(Flake8Task.class);
    private PackageSettings<PackageInfo> packageSettings;

    @Override
    PythonDetails getPythonDetails() {
        return new PythonDetails(getProject(), new File(getProject().getBuildDir(), "flake8-venv"));

    }

    public void preExecution() {
        PythonDetails flake8Python = getPythonDetails();

        File flake8Exec = flake8Python.getVirtualEnvironment().findExecutable("flake8");
        if (!flake8Exec.exists() || !flake8Exec.isFile()) {

            // We don't know what happened to this venv, so lets kill it
            if (flake8Python.getVirtualEnv().exists()) {
                FileUtils.deleteQuietly(flake8Python.getVirtualEnv());
            }

            CreateVirtualEnvAction action = new CreateVirtualEnvAction(getProject(), flake8Python);
            action.buildVenv(null);

            PipInstallAction pipInstallAction = new PipInstallAction(packageSettings, getProject(),
                new ProjectExternalExec(getProject()), getPythonExtension().pythonEnvironment,
                flake8Python, new EmptyWheelCache(), new DefaultEnvironmentMerger());

            getProject().getConfigurations().getByName("flake8").forEach(file -> {
                PackageInfo packageInfo = PackageInfo.fromPath(file);
                log.info("Installing {}", packageInfo);
                pipInstallAction.installPackage(packageInfo, Collections.emptyList());
            });
        }

        /*
         Modified to only include folders that exist. if no folders exist, then
         the task isn't actually run.
         */
        PythonExtension pythonExtension = getPythonExtension();
        List<String> sArgs = Arrays.asList(
            flake8Exec.getAbsolutePath(),
            "--config", pythonExtension.setupCfg);

        List<String> paths = new ArrayList<>();
        if (getProject().file(pythonExtension.srcDir).exists()) {
            log.info("Flake8: adding {}", pythonExtension.srcDir);
            paths.add(pythonExtension.srcDir);
        } else {
            log.info("Flake8: srcDir doesn't exist");
        }

        if (getProject().file(pythonExtension.testDir).exists()) {
            log.info("Flake8: adding {}", pythonExtension.testDir);
            paths.add(pythonExtension.testDir);
        } else {
            log.info("Flake8: testDir doesn't exist");
        }
        args(sArgs);

        // creating a flake8 config file if one doesn't exist, this prevents "file not found" issues.
        File cfgCheck = getProject().file(pythonExtension.setupCfg);
        if (!cfgCheck.exists()) {
            log.info("Flake8 config file doesn't exist, creating default");
            try {
                FileUtils.write(cfgCheck, "[flake8]");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            log.info("Flake8 config file exists");
        }

        args(paths);
    }

    @Override
    public void processResults(ExecResult execResult) {
    }

    @Override
    public void setPackageSettings(PackageSettings<PackageInfo> settings) {
        packageSettings = settings;
    }

    @Override
    public PackageSettings<PackageInfo> getPackageSettings() {
        return packageSettings;
    }
}
