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
import com.linkedin.gradle.python.tasks.action.VirtualEnvCustomizer;
import com.linkedin.gradle.python.tasks.action.pip.PipInstallAction;
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec;
import com.linkedin.gradle.python.tasks.supports.SupportsDistutilsCfg;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings;
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.EmptyWheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.process.ExecResult;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Flake8Task extends AbstractPythonInfrastructureDefaultTask implements SupportsPackageInfoSettings,
    SupportsDistutilsCfg {

    private static final Logger log = Logging.getLogger(Flake8Task.class);
    private PackageSettings<PackageInfo> packageSettings;
    private EnvironmentMerger environmentMerger = new DefaultEnvironmentMerger();

    @Input
    @Optional
    private String distutilsCfg;

    @Override
    public PythonDetails getPythonDetails() {
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

            ProjectExternalExec externalExec = new ProjectExternalExec(getProject());

            CreateVirtualEnvAction action = new CreateVirtualEnvAction(getProject(), flake8Python);
            action.buildVenv(new VirtualEnvCustomizer(distutilsCfg, externalExec, flake8Python));

            PipInstallAction pipInstallAction = new PipInstallAction(packageSettings, getProject(),
                externalExec, getPythonExtension().pythonEnvironment,
                flake8Python, new EmptyWheelCache(), environmentMerger);

            getProject().getConfigurations().getByName("flake8").forEach(file -> {
                PackageInfo packageInfo = PackageInfo.fromPath(file);
                pipInstallAction.installPackage(packageInfo, Collections.emptyList());
            });
        }

        /*
         Modified to only include folders that exist. if no folders exist, then
         the task isn't actually run.
         */
        PythonExtension pythonExtension = getPythonExtension();

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
        args(Arrays.asList(
            flake8Exec.getAbsolutePath(),
            "--config", pythonExtension.setupCfg));

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
        //Not needed
    }

    @Override
    public void setPackageSettings(PackageSettings<PackageInfo> settings) {
        packageSettings = settings;
    }

    @Override
    public PackageSettings<PackageInfo> getPackageSettings() {
        return packageSettings;
    }

    @Override
    public void setDistutilsCfg(String cfg) {
        distutilsCfg = cfg;
    }

    @Override
    public String getDistutilsCfg() {
        return distutilsCfg;
    }
}
