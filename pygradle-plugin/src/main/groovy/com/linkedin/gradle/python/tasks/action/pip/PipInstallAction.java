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

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.exception.PipExecutionException;
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.OperatingSystem;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class PipInstallAction extends AbstractPipAction {

    private static Logger logger = Logging.getLogger(PipInstallAction.class);

    private final Path sitePackagesPath;
    private final WheelBuilder wheelBuilder;

    public PipInstallAction(PackageSettings<PackageInfo> packageSettings,
                            Project project,
                            ExternalExec externalExec, Map<String, String> baseEnvironment,
                            PythonDetails pythonDetails,
                            WheelCache wheelCache, EnvironmentMerger environmentMerger,
                            Spec<PackageInfo> packageExcludeFilter) {
        super(packageSettings, project, externalExec, baseEnvironment, pythonDetails, wheelCache,
                environmentMerger, packageExcludeFilter);
        this.sitePackagesPath = findSitePackages(pythonDetails);
        this.wheelBuilder = new WheelBuilder(packageSettings, project, externalExec, baseEnvironment, pythonDetails,
                wheelCache, environmentMerger, packageExcludeFilter);
    }

    public Path getSitePackagesPath() {
        return sitePackagesPath;
    }

    private static Path findSitePackages(PythonDetails pythonDetails) {
        String pyVersion = pythonDetails.getPythonVersion().getPythonMajorMinor();
        if (OperatingSystem.current().isUnix()) {
            return pythonDetails.getVirtualEnv().toPath().resolve(Paths.get("lib", "python" + pyVersion, "site-packages"));
        } else {
            return pythonDetails.getVirtualEnv().toPath().resolve(Paths.get("Lib", "site-packages"));
        }
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    @Override
    void doPipOperation(PackageInfo packageInfo, List<String> extraArgs) {
        throwIfPythonVersionIsNotSupported(packageInfo);

        String pyVersion = pythonDetails.getPythonVersion().getPythonMajorMinor();
        String sanitizedName = packageInfo.getName().replace('-', '_');

        // See: https://www.python.org/dev/peps/pep-0376/
        File egg = sitePackagesPath.resolve(sanitizedName + "-" + packageInfo.getVersion() + "-py" + pyVersion + ".egg-info").toFile();
        File dist = sitePackagesPath.resolve(sanitizedName + "-" + packageInfo.getVersion() + ".dist-info").toFile();

        if (!packageSettings.requiresSourceBuild(packageInfo)
                && (project.file(egg).exists() || project.file(dist).exists())) {
            if (PythonHelpers.isPlainOrVerbose(project)) {
                logger.lifecycle("Skipping {} - Installed", packageInfo.toShortHand());
            }
            wheelBuilder.updateWheelReadiness(packageInfo);
            return;
        }

        Map<String, String> mergedEnv = environmentMerger.mergeEnvironments(
            Arrays.asList(baseEnvironment, packageSettings.getEnvironment(packageInfo)));


        List<String> commandLine = makeCommandLine(packageInfo, extraArgs);

        if (PythonHelpers.isPlainOrVerbose(project)) {
            logger.lifecycle("Installing {}", packageInfo.toShortHand());
        }

        OutputStream stream = new ByteArrayOutputStream();
        ExecResult installResult = execCommand(mergedEnv, commandLine, stream);

        String message = stream.toString().trim();
        if (installResult.getExitValue() != 0) {
            /*
             * TODO: maintain a list of packages that failed to install, and report a failure
             * report at the end. We can leverage our domain expertise here to provide very
             * meaningful errors. E.g., we see lxml failed to install, do you have libxml2
             * installed? E.g., we see pyOpenSSL>0.15 failed to install, do you have libffi
             * installed?
             */
            logger.error("Error installing package using `{}`", commandLine);
            logger.error(message);
            throw PipExecutionException.failedInstall(packageInfo, message);
        } else {
            logger.info(message);
        }
    }

    private List<String> makeCommandLine(PackageInfo packageInfo, List<String> extraArgs) {
        List<String> commandLine = new ArrayList<>();
        commandLine.addAll(baseInstallArguments());
        commandLine.addAll(extraArgs);
        commandLine.addAll(getGlobalOptions(packageInfo));
        commandLine.addAll(getInstallOptions(packageInfo));
        commandLine.add(wheelBuilder.getPackage(packageInfo, extraArgs).toString());

        return commandLine;
    }

    private List<String> baseInstallArguments() {
        return Arrays.asList(
            pythonDetails.getVirtualEnvInterpreter().toString(),
            pythonDetails.getVirtualEnvironment().getPip().toString(),
            "install",
            "--disable-pip-version-check",
            "--no-deps");
    }
}
