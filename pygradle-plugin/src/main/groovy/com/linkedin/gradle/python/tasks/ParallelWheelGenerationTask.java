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
import com.linkedin.gradle.python.extension.PlatformTag;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.extension.PythonTag;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.internal.TaskTimer;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.process.ExecResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelWheelGenerationTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(ParallelWheelGenerationTask.class);

    private FileCollection filesToConvert;
    private File cacheDir;
    private PythonExtension extension;
    private AtomicInteger counter = new AtomicInteger();

    @TaskAction
    public void buildWheels() {

        ProgressLoggerFactory progressLoggerFactory = getServices().get(ProgressLoggerFactory.class);
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(ParallelWheelGenerationTask.class);
        progressLogger.setDescription("Building wheels");

        PythonTag pythonTag = PythonTag.findTag(getProject(), getPythonDetails());
        PlatformTag platformTag = PlatformTag.makePlatformTag(getProject(), getPythonDetails());

        WheelCache wheelCache = new WheelCache(cacheDir, pythonTag, platformTag);

        progressLogger.started();

        TaskTimer taskTimer = new TaskTimer();

        Set<File> files = getFilesToConvert().getFiles();
        int totalSize = files.size();
        files.parallelStream().forEach(file -> {
            PackageInfo packageInfo = PackageInfo.fromPath(file.getPath());
            TaskTimer.TickingClock clock = taskTimer.start(packageInfo.getName() + "-" + packageInfo.getVersion());
            makeWheelFromSdist(progressLogger, totalSize, wheelCache, file);
            clock.stop();
        });

        try {
            FileUtils.write(getBuildReport(), taskTimer.buildReport());
        } catch (IOException ignore) {
            // Don't fail if there is are issues writing the timing report.
        }
        progressLogger.completed();
    }

    private void makeWheelFromSdist(ProgressLogger progressLogger, int totalSize, WheelCache wheelCache, File input) {

        if (input.getName().endsWith(".whl")) {
            return;
        }

        PackageInfo packageInfo = PackageInfo.fromPath(input.getPath());
        progressLogger.progress(String.format("Building wheel %s %d of %d", packageInfo.getName(), counter.incrementAndGet(), totalSize));
        Optional<File> cachedWheel = wheelCache.findWheel(packageInfo.getName(), packageInfo.getVersion(), getPythonDetails().getPythonVersion());

        if (cachedWheel.isPresent()) {
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
                input.getAbsoluteFile().getAbsolutePath());
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
        }
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


}
