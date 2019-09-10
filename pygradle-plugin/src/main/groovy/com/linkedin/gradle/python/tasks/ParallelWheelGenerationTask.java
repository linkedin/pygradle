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
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageFiltering;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.util.internal.TaskTimer;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.process.ExecResult;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_SETUP_REQS;

public class ParallelWheelGenerationTask extends DefaultTask implements SupportsPackageInfoSettings, SupportsPackageFiltering {

    private static final Logger logger = Logging.getLogger(ParallelWheelGenerationTask.class);

    private WheelCache wheelCache;

    private Set<String> currentPackages = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private FileCollection filesToConvert;
    private File cacheDir;
    private PythonExtension extension;
    private AtomicInteger counter = new AtomicInteger();

    private PackageSettings<PackageInfo> packageSettings;
    private Spec<PackageInfo> packageFilter;

    public ParallelWheelGenerationTask() {
        onlyIf(task -> {
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            getProject().exec(execSpec -> {
                execSpec.setExecutable(getPythonDetails().getVirtualEnvInterpreter());
                execSpec.args(getPythonDetails().getVirtualEnvironment().getPip(), "freeze", "--all");
                execSpec.setStandardOutput(stdOut);
            });

            Configuration requiredDependencies = getProject().getConfigurations()
                .getByName(CONFIGURATION_SETUP_REQS.getValue());

            Set<String> setupRequiresDependencies = requiredDependencies.getIncoming().getDependencies().stream()
                .flatMap(it -> Stream.of(it.getName(), it.getName().replace("-", "_")))
                .collect(Collectors.toSet());

            Set<String> extraDependencies = Arrays.stream(stdOut.toString().trim().split(System.lineSeparator()))
                .filter(it -> it.contains("==")).map(it -> it.split("==")[0])
                .filter(it -> !setupRequiresDependencies.contains(it))
                .collect(Collectors.toSet());

            if (!extraDependencies.isEmpty()) {
                logger.info("Extra dependencies found ({}). Skipping parallel wheel building.", extraDependencies);
            }

            return extraDependencies.isEmpty();
        });
    }

    @TaskAction
    public void buildWheels() {

        ProgressLoggerFactory progressLoggerFactory = getServices().get(ProgressLoggerFactory.class);
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(ParallelWheelGenerationTask.class);
        progressLogger.setDescription("Building wheels");

        progressLogger.started();

        TaskTimer taskTimer = new TaskTimer();

        // This way we don't try to over-alloc the system to much. We'll use slightly over 1/2 of the machine to build
        // the wheels in parallel. Allowing other operations to continue.
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() / 2 + 1);

        Set<File> files = getFilesToConvert().getFiles();
        int totalSize = files.size();

        try {
            forkJoinPool.submit(() -> {
                files.stream().parallel().forEach(file -> {

                    PackageInfo packageInfo = PackageInfo.fromPath(file);
                    currentPackages.add(packageInfo.getName());
                    counter.incrementAndGet();
                    updateStatusLine(progressLogger, totalSize, counter.get());
                    TaskTimer.TickingClock clock = taskTimer.start(packageInfo.getName() + "-" + packageInfo.getVersion());
                    if (!packageSettings.requiresSourceBuild(packageInfo)) {
                        makeWheelFromSdist(packageInfo);
                    }
                    currentPackages.remove(packageInfo.getName());
                    updateStatusLine(progressLogger, totalSize, counter.get());
                    clock.stop();
                });
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Unable to pre-build some dependencies");
        } finally {
            forkJoinPool.shutdown();
        }

        try {
            FileUtils.write(getBuildReport(), taskTimer.buildReport());
        } catch (IOException ignore) {
            // Don't fail if there is are issues writing the timing report.
        }
        progressLogger.completed();
    }

    private void makeWheelFromSdist(PackageInfo packageInfo) {

        if (packageInfo.getPackageFile().getName().endsWith(".whl")) {
            return;
        }

        if (packageFilter != null && packageFilter.isSatisfiedBy(packageInfo)) {
            if (PythonHelpers.isPlainOrVerbose(getProject())) {
                logger.lifecycle("Skipping building {} wheel - Excluded", packageInfo.toShortHand());
            }
            return;
        }

        Optional<File> cachedWheel = wheelCache.findWheel(
            packageInfo.getName(),
            packageInfo.getVersion(),
            getPythonDetails());

        if (cachedWheel.isPresent()) {
            if (PythonHelpers.isPlainOrVerbose(getProject())) {
                logger.lifecycle("Wheel for {}-{} was found: {}",
                    packageInfo.getName(), packageInfo.getVersion(), cachedWheel.get());
            }
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ExecResult results = getProject().exec(exec -> {
            exec.commandLine(getPythonDetails().getVirtualEnvInterpreter(),
                getPythonDetails().getVirtualEnvironment().getPip(),
                "wheel",
                "--disable-pip-version-check",
                "--wheel-dir", cacheDir,
                "--no-deps",
                packageInfo.getPackageFile().getAbsoluteFile().getAbsolutePath());
            exec.setStandardOutput(stream);
            exec.setErrorOutput(stream);
            exec.setIgnoreExitValue(true);
        });

        if (results.getExitValue() != 0) {
            logger.info("Unable to build wheel for {}-{}", packageInfo.getName(), packageInfo.getVersion());
            File resultDir = new File(getProject().getBuildDir(), getName() + "-" + packageInfo.getName() + "-" + packageInfo.getVersion() + ".txt");
            try {
                FileUtils.write(resultDir, stream.toString());
            } catch (IOException ignored) {
                // Don't fail if there is are issues writing the wheel report.
            }
        } else {
            if (PythonHelpers.isPlainOrVerbose(getProject())) {
                logger.lifecycle("Wheel was built for {}-{}", packageInfo.getName(), packageInfo.getVersion());
            }
        }
    }

    private void updateStatusLine(ProgressLogger progressLogger, int totalSize, int currentPackageCount) {
        String packagesBeingBuilt = currentPackages.stream().collect(Collectors.joining(", "));
        progressLogger.progress(String.format("Building wheel(s) [ %s ] %d of %d", packagesBeingBuilt, currentPackageCount, totalSize));
    }

    @Internal
    public PythonExtension getExtension() {
        if (null == extension) {
            extension = getProject().getExtensions().getByType(PythonExtension.class);
        }
        return extension;
    }

    @Internal
    public PythonDetails getPythonDetails() {
        return getExtension().getDetails();
    }

    @OutputFile
    public File getBuildReport() {
        return new File(getProject().getBuildDir(), "wheel-build.txt");
    }

    @OutputDirectory
    public File getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    @InputFiles
    public FileCollection getFilesToConvert() {
        return filesToConvert;
    }

    public void setFilesToConvert(FileCollection filesToConvert) {
        this.filesToConvert = filesToConvert;
    }

    @Input
    public WheelCache getWheelCache() {
        return wheelCache;
    }

    public void setWheelCache(WheelCache wheelCache) {
        this.wheelCache = wheelCache;
    }

    @Override
    public void setPackageSettings(PackageSettings<PackageInfo> settings) {
        this.packageSettings = settings;
    }

    @Override
    public PackageSettings<PackageInfo> getPackageSettings() {
        return this.packageSettings;
    }

    @Nullable
    @Override
    public Spec<PackageInfo> getPackageExcludeFilter() {
        return packageFilter;
    }

    @Override
    public void setPackageExcludeFilter(Spec<PackageInfo> filter) {
        packageFilter = filter;
    }
}
