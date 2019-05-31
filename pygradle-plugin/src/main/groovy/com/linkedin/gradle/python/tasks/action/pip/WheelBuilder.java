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
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.WheelCache;
import com.linkedin.gradle.python.wheel.WheelCacheLayer;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Action class that handles wheel building and fetching from cache.
 *
 * This class is used from PipInstallAction and does not perform the
 * checks already done in that class. It does nicely separate the concern
 * of wheel building and finding from PipInstallAction and ensures
 * it returns back the package file in some form while leaving
 * the wheel in at least one cache layer.
 */
public class WheelBuilder extends AbstractPipAction {
    // Options for "pip install" that do not work with "pip wheel" command.
    private static final List<String> NOT_WHEEL_OPTIONS = Arrays.asList("--upgrade", "--ignore-installed");

    // Environment variables used for a specific package only and customizing its build.
    private static final Map<String, List<String>> CUSTOM_ENVIRONMENT = Collections.unmodifiableMap(Stream.of(
        new AbstractMap.SimpleEntry<>("numpy", Arrays.asList("ATLAS", "BLAS", "LAPACK", "OPENBLAS")),
        new AbstractMap.SimpleEntry<>("pycurl", Collections.singletonList("PYCURL_SSL_LIBRARY"))
    ).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

    private static final Logger logger = Logging.getLogger(WheelBuilder.class);

    private boolean customBuild = true;

    // package-private
    WheelBuilder(PackageSettings<PackageInfo> packageSettings,
                        Project project,
                        ExternalExec externalExec,
                        Map<String, String> baseEnvironment,
                        PythonDetails pythonDetails,
                        WheelCache wheelCache,
                        EnvironmentMerger environmentMerger,
                        Spec<PackageInfo> packageExcludeFilter) {
        super(packageSettings, project, externalExec, baseEnvironment,
                pythonDetails, wheelCache, environmentMerger, packageExcludeFilter);
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    /*
     * Since WheelBuilder class is always called from PipInstallAction,
     * and that class already performed all the filtering checks,
     * we're overriding the original execute method from the abstract class
     * to avoid duplicate checks. It becomes just a call to doPipOperation,
     * but we have to implement that abstract method.
     */
    @Override
    public void execute(PackageInfo packageInfo, List<String> extraArgs) {
        doPipOperation(packageInfo, extraArgs);
    }

    /*
     * Similar to execute method above, we're skipping some checks already
     * done in PipInstallAction where WheelBuilder is called from.
     * For example, the check for supported versions.
     * Some other checks, such as source build requirement, are moved to
     * the main method of the wheel builder -- getPackage.
     */
    @Override
    void doPipOperation(PackageInfo packageInfo, List<String> extraArgs) {
        List<String> commandLine = makeCommandLine(packageInfo, extraArgs);
        if (commandLine.isEmpty()) {
            return;
        }

        if (PythonHelpers.isPlainOrVerbose(project)) {
            logger.lifecycle("Building wheel for {}", packageInfo.toShortHand());
        }

        Map<String, String> mergedEnv;
        // The flag is set in getPackage method.
        if (customBuild) {
            mergedEnv = environmentMerger.mergeEnvironments(
                Arrays.asList(baseEnvironment, packageSettings.getEnvironment(packageInfo)));
        } else {
            // Have to use this for customized environments that are explicitly marked non-customized
            mergedEnv = packageSettings.getEnvironment(packageInfo);
        }

        OutputStream stream = new ByteArrayOutputStream();

        ExecResult installResult = execCommand(mergedEnv, commandLine, stream);

        if (installResult.getExitValue() == 0) {
            logger.info(stream.toString().trim());
        } else {
            logger.error("Error building package wheel using `{}`", commandLine);
            logger.error(stream.toString().trim());
            throw PipExecutionException.failedWheel(packageInfo, stream.toString().trim());
        }

    }

    // package-private
    File getPackage(PackageInfo packageInfo, List<String> extraArgs) {
        File packageFile = packageInfo.getPackageFile();

        // Cut it short if there's no target directory in the cache.
        if (!wheelCache.getTargetDirectory().isPresent()) {
            return packageFile;
        }

        // Project wheel will be built after the editable install for development.
        if (isPackageDirectory(packageInfo) && project.getProjectDir().equals(packageFile)) {
            return packageFile;
        }

        String name = packageInfo.getName();
        String version = packageInfo.getVersion();
        Optional<File> wheel;

        /*
         * The generated code, such as rest.li can be a path to directory.
         * In that case the version will be null and we need to set it to
         * project's version.
         */
        if (isPackageDirectory(packageInfo)) {
            version = project.getVersion().toString();
        }

        /*
         * Safety belt.
         * This should be impossible and prevented by PackageInfo parsing already.
         * However, if either of name/version is still null, return the original back.
         */
        if (name == null || version == null) {
            return packageFile;
        }

        // set the flag for doPipOperation
        customBuild = packageSettings.requiresSourceBuild(packageInfo)
                || packageSettings.isCustomized(packageInfo)
                || isCustomEnvironment(name);

        // Look in cache layers first when applicable.
        if (!packageSettings.requiresSourceBuild(packageInfo)) {
            wheel = wheelCache.findWheel(name, version, pythonDetails, WheelCacheLayer.PROJECT_LAYER);
            if (wheel.isPresent()) {
                packageFile = wheel.get();
                logLifecycle(packageInfo, packageFile);
                return packageFile;
            } else if (!customBuild) {
                wheel = wheelCache.findWheel(name, version, pythonDetails, WheelCacheLayer.HOST_LAYER);
                if (wheel.isPresent()) {
                    packageFile = wheel.get();
                    wheelCache.storeWheel(packageFile, WheelCacheLayer.PROJECT_LAYER);
                    logLifecycle(packageInfo, packageFile);
                    return packageFile;
                }
            }
        }

        // Build the wheel into the project layer by default.
        try {
            execute(packageInfo, extraArgs);
        } catch (PipExecutionException e) {
            if (!customBuild) {
                /*
                 * The users may need PythonEnvironment for their wheel build.
                 * We must treat this as a custom build and set a flag for
                 * doPipOperation to merge PythonEnvironment in accordingly.
                 * Then retry.
                 */
                customBuild = true;
                try {
                    execute(packageInfo, extraArgs);
                } catch (PipExecutionException ignored) {
                    wheelCache.setWheelsReady(false);
                }
            } else {
                wheelCache.setWheelsReady(false);
            }
        }

        wheel = wheelCache.findWheel(name, version, pythonDetails, WheelCacheLayer.PROJECT_LAYER);
        if (wheel.isPresent()) {
            packageFile = wheel.get();
            if (!customBuild) {
                wheelCache.storeWheel(packageFile, WheelCacheLayer.HOST_LAYER);
            }
        }

        return packageFile;
    }

    /**
     * Update the flag for wheel readiness.
     *
     * <p>This method is called to ensure the wheel is present in the
     * wheel cache when we expect it to be there. If the wheel is not
     * found, we'll drop the wheels ready flag to indicate that some
     * wheels need to be rebuilt. The dropped flag will trigger full
     * execution of buildWheels task later and, thus, ensure that all
     * the wheels needed for the deployable zipapp are present when
     * it's being packaged.</p>
     *
     * <p>Here's one possible scenario where this kind of update is
     * necessary. When a user deletes wheels or the whole wheel-cache
     * manually, but does not delete the virtualenv, we may find the
     * package in the virtualenv, while it may be missing in the wheel
     * cache.</p>
     *
     * <p>The update is skipped if the flag is already dropped.</p>
     */
    // package-private
    void updateWheelReadiness(PackageInfo packageInfo) {
        if (wheelCache.isWheelsReady()) {
            String name = packageInfo.getName();
            String version = packageInfo.getVersion();
            Optional<File> wheel;

            if (name == null || version == null) {
                wheel = Optional.empty();
            } else {
                wheel = wheelCache.findWheel(name, version, pythonDetails, WheelCacheLayer.PROJECT_LAYER);
            }

            if (!wheel.isPresent()) {
                wheelCache.setWheelsReady(false);
            }
        }
    }

    private List<String> makeCommandLine(PackageInfo packageInfo, List<String> extraArgs) {
        List<String> commandLine = new ArrayList<>();
        Optional<File> targetDir = wheelCache.getTargetDirectory();

        if (targetDir.isPresent()) {
            String wheelDirPath = targetDir.get().toString();

            commandLine.addAll(Arrays.asList(
                pythonDetails.getVirtualEnvInterpreter().toString(),
                pythonDetails.getVirtualEnvironment().getPip().toString(),
                "wheel",
                "--disable-pip-version-check",
                "--wheel-dir", wheelDirPath,
                "--no-deps"
            ));
            commandLine.addAll(cleanupArgs(extraArgs));
            commandLine.addAll(getGlobalOptions(packageInfo));
            commandLine.addAll(getBuildOptions(packageInfo));

            commandLine.add(packageInfo.getPackageFile().toString());
        }

        return commandLine;
    }

    private List<String> cleanupArgs(List<String> args) {
        List<String> cleanArgs = new ArrayList<>(args);
        cleanArgs.removeAll(NOT_WHEEL_OPTIONS);
        return cleanArgs;
    }

    private boolean isPackageDirectory(PackageInfo packageInfo) {
        File packageDir = packageInfo.getPackageFile();
        String version = packageInfo.getVersion();
        return version == null && Files.isDirectory(packageDir.toPath());
    }

    // Use of pythonEnvironment may hide really customized packages. Catch them!
    private boolean isCustomEnvironment(String name) {
        if (CUSTOM_ENVIRONMENT.containsKey(name)) {
            for (String entry : CUSTOM_ENVIRONMENT.get(name)) {
                if (baseEnvironment.containsKey(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void logLifecycle(PackageInfo packageInfo, File packageFile) {
        if (PythonHelpers.isPlainOrVerbose(project)) {
            logger.lifecycle("{} from wheel: {}",
                packageInfo.toShortHand(), packageFile.getAbsolutePath());
        }
    }
}
