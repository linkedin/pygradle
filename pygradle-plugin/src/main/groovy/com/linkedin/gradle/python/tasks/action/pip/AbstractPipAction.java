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
package com.linkedin.gradle.python.tasks.action.pip;

import com.linkedin.gradle.python.exception.PipExecutionException;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.extension.PythonVersion;
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.specs.Spec;
import org.gradle.process.ExecResult;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class AbstractPipAction {

    final PackageSettings<PackageInfo> packageSettings;
    final Project project;
    final ExternalExec externalExec;
    final Map<String, String> baseEnvironment;
    final PythonDetails pythonDetails;
    final WheelCache wheelCache;
    final EnvironmentMerger environmentMerger;
    final PythonVersion pythonVersion;
    final Spec<PackageInfo> packageExcludeFilter;

    AbstractPipAction(PackageSettings<PackageInfo> packageSettings,
                      Project project,
                      ExternalExec externalExec,
                      Map<String, String> baseEnvironment,
                      PythonDetails pythonDetails,
                      WheelCache wheelCache,
                      EnvironmentMerger environmentMerger,
                      Spec<PackageInfo> packageExcludeFilter) {
        this.packageSettings = packageSettings;
        this.project = project;
        this.externalExec = externalExec;
        this.baseEnvironment = baseEnvironment;
        this.pythonDetails = pythonDetails;
        this.wheelCache = wheelCache;
        this.environmentMerger = environmentMerger;
        this.pythonVersion = pythonDetails.getPythonVersion();
        this.packageExcludeFilter = packageExcludeFilter;
    }

    public void execute(PackageInfo packageInfo, List<String> extraArgs) {
        if (packageExcludeFilter != null && packageExcludeFilter.isSatisfiedBy(packageInfo)) {
            if (PythonHelpers.isPlainOrVerbose(project)) {
                getLogger().lifecycle("Skipping {} - Excluded", packageInfo.toShortHand());
            }
            return;
        }

        doPipOperation(packageInfo, extraArgs);
    }

    void throwIfPythonVersionIsNotSupported(PackageInfo packageInfo) {
        // If supported versions are empty, there are no restrictions.
        List<String> supportedVersions = packageSettings.getSupportedLanguageVersions(packageInfo);
        if (supportedVersions != null && !supportedVersions.isEmpty()
            && !supportedVersions.contains(pythonVersion.getPythonMajorMinor())) {
            throw PipExecutionException.unsupportedPythonVersion(packageInfo, supportedVersions);
        }
    }

    ExecResult execCommand(Map<String, String> mergedEnv, List<String> commandLine, OutputStream stream) {
        return externalExec.exec(execSpec -> {
            execSpec.environment(mergedEnv);
            execSpec.commandLine(commandLine);
            execSpec.setStandardOutput(stream);
            execSpec.setErrorOutput(stream);
            execSpec.setIgnoreExitValue(true);
        });
    }

    /**
     * @return Always returns a list, never null.
     */
    List<String> getGlobalOptions(PackageInfo packageInfo) {
        List<String> globalOptions = packageSettings.getGlobalOptions(packageInfo);
        if (globalOptions == null) {
            return Collections.emptyList();
        } else {
            return globalOptions;
        }
    }

    /**
     * @return Always returns a list, never null.
     */
    List<String> getInstallOptions(PackageInfo packageInfo) {
        List<String> installOptions = packageSettings.getInstallOptions(packageInfo);
        if (installOptions == null) {
            return Collections.emptyList();
        } else {
            return installOptions;
        }
    }

    /**
     * @return Always returns a list, never null.
     */
    List<String> getBuildOptions(PackageInfo packageInfo) {
        List<String> buildOptions = packageSettings.getBuildOptions(packageInfo);
        if (buildOptions == null) {
            return Collections.emptyList();
        } else {
            return buildOptions;
        }
    }

    abstract void doPipOperation(PackageInfo packageInfo, List<String> extraArgs);

    abstract Logger getLogger();
}
