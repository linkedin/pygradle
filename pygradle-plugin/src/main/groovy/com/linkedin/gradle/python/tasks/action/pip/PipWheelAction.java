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
import com.linkedin.gradle.python.extension.WheelExtension;
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PipWheelAction extends AbstractPipAction {

    private static Logger logger = Logging.getLogger(PipWheelAction.class);
    private final WheelExtension wheelExtension;

    public PipWheelAction(PackageSettings<PackageInfo> packageSettings,
                          Project project,
                          ExternalExec externalExec,
                          Map<String, String> baseEnvironment,
                          PythonDetails pythonDetails,
                          WheelCache wheelCache,
                          EnvironmentMerger environmentMerger,
                          WheelExtension wheelExtension,
                          Spec<PackageInfo> packageExcludeFilter) {
        super(packageSettings, project, externalExec, baseEnvironment, pythonDetails, wheelCache,
            environmentMerger, packageExcludeFilter);
        this.wheelExtension = wheelExtension;
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    @Override
    void doPipOperation(PackageInfo packageInfo, List<String> extraArgs) {
        throwIfPythonVersionIsNotSupported(packageInfo);

        if (!packageSettings.requiresSourceBuild(packageInfo) && doesWheelExist(packageInfo)) {
            return;
        }

        if (PythonHelpers.isPlainOrVerbose(project)) {
            logger.lifecycle("Building {} wheel", packageInfo.toShortHand());
        }

        Map<String, String> mergedEnv = environmentMerger.mergeEnvironments(
            Arrays.asList(baseEnvironment, packageSettings.getEnvironment(packageInfo)));

        List<String> commandLine = makeCommandLine(packageInfo, extraArgs);

        OutputStream stream = new ByteArrayOutputStream();

        ExecResult installResult = execCommand(mergedEnv, commandLine, stream);

        if (installResult.getExitValue() != 0) {
            logger.error("Error installing package using `{}`", commandLine);
            logger.error(stream.toString().trim());
            throw PipExecutionException.failedWheel(packageInfo, stream.toString().trim());
        } else {
            logger.info(stream.toString().trim());
        }

    }

    /*
     * Check if a wheel exists for this product already and only build it
     * if it is missing. We don't care about the wheel details because we
     * always build these locally.
     */
    private boolean doesWheelExist(PackageInfo packageInfo) {
        if (!packageSettings.isCustomized(packageInfo)) {
            Optional<File> wheel = wheelCache.findWheel(packageInfo.getName(), packageInfo.getVersion(), pythonDetails);
            if (wheel.isPresent()) {
                File wheelFile = wheel.get();
                File wheelCopy = new File(wheelExtension.getWheelCache(), wheelFile.getName());

                if (!wheelFile.equals(wheelCopy)) {
                    try {
                        FileUtils.copyFile(wheelFile, wheelCopy);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                if (PythonHelpers.isPlainOrVerbose(project)) {
                    logger.lifecycle("Skipping {}, in wheel cache {}", packageInfo.toShortHand(), wheelFile);
                }
                return true;
            }
        }

        ConfigurableFileTree tree = project.fileTree(wheelExtension.getWheelCache(), action -> {
            String sanitizedName = packageInfo.getName().replace('-', '_');
            String sanitizedVersion = (packageInfo.getVersion() == null ? "unspecified" : packageInfo.getVersion()).replace('-', '_');
            action.include("**/" + sanitizedName + "-" + sanitizedVersion + "-*.whl");
        });

        if (tree.getFiles().size() >= 1) {
            if (PythonHelpers.isPlainOrVerbose(project)) {
                logger.lifecycle("Skipping {} wheel - Installed", packageInfo.toShortHand());
            }
            return true;
        }
        return false;
    }

    private List<String> makeCommandLine(PackageInfo packageInfo, List<String> extraArgs) {
        List<String> commandLine = new ArrayList<>();
        commandLine.addAll(Arrays.asList(
            pythonDetails.getVirtualEnvInterpreter().toString(),
            pythonDetails.getVirtualEnvironment().getPip().toString(),
            "wheel",
            "--disable-pip-version-check",
            "--wheel-dir", wheelExtension.getWheelCache().toString(),
            "--no-deps"));

        commandLine.addAll(extraArgs);
        commandLine.addAll(getGlobalOptions(packageInfo));
        commandLine.addAll(getBuildOptions(packageInfo));

        commandLine.add(packageInfo.getPackageFile().toString());

        return commandLine;
    }
}
