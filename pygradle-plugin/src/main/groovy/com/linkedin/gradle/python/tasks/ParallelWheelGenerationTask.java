package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.PlatformTag;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.extension.PythonTag;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelWheelGenerationTask extends DefaultTask {

    private FileCollection filesToConvert;
    private File cacheDir;
    private PythonExtension extension;
    private PythonDetails pythonDetails;
    private AtomicInteger counter = new AtomicInteger();

    @TaskAction
    public void buildWheels() {

        ProgressLoggerFactory progressLoggerFactory = getServices().get(ProgressLoggerFactory.class);
        ProgressLogger progressLogger = progressLoggerFactory.newOperation(ParallelWheelGenerationTask.class);
        progressLogger.setDescription("Building wheels");

        PythonTag pythonTag = PythonTag.findTag(getProject(), getPythonDetails());
        PlatformTag platformTag;
        if ("2".equals(getPythonDetails().getPythonVersion().getPythonMajor())) {

            platformTag = new PlatformTag("none");
        } else {
            platformTag = PlatformTag.makePlatformTag(getProject(), getPythonDetails().getVirtualEnvInterpreter());
        }

        WheelCache wheelCache = new WheelCache(cacheDir, pythonTag, platformTag);

        progressLogger.started();

        Map<String, Long> wheelBuildTiming = new HashMap<>();

        Set<File> files = getFilesToConvert().getFiles();
        int totalSize = files.size();
        files.parallelStream().forEach(file -> {
            PackageInfo packageInfo = PackageInfo.fromPath(file.getPath());
            long start = System.currentTimeMillis();
            makeWheelFromSdist(progressLogger, totalSize, wheelCache, file);
            long end = System.currentTimeMillis();

            wheelBuildTiming.put(packageInfo.toString(), end - start);
        });
        StringBuilder sw = new StringBuilder();
        wheelBuildTiming.forEach((key, value) -> sw.append(key).append(": ").append(value).append("\n"));

        try {
            FileUtils.write(getBuildReport(), sw.toString());
        } catch (IOException ignore) {
        }
        progressLogger.completed();
    }

    private void makeWheelFromSdist(ProgressLogger progressLogger, int totalSize, WheelCache wheelCache, File input) {

        if (input.getName().endsWith(".whl")) {
            return;
        }

        PackageInfo packageInfo = PackageInfo.fromPath(input.getPath());
        progressLogger.progress(String.format("Building wheel %s %d of %d", packageInfo.getName(), counter.incrementAndGet(), totalSize));
        Optional<File> cachedWheel = wheelCache.findWheel(packageInfo.getName(), packageInfo.getVersion(), pythonDetails.getPythonVersion());

        if (cachedWheel.isPresent()) {
            return;
        }

        getProject().exec(exec -> {
            exec.commandLine(pythonDetails.getVirtualEnvInterpreter(),
                pythonDetails.getVirtualEnvironment().getPip(),
                "wheel",
                "--disable-pip-version-check",
                "--wheel-dir", cacheDir,
                "--no-deps",
                input.getAbsoluteFile().getAbsolutePath());
            exec.setStandardOutput(new IgnoreOutputStream());
            exec.setErrorOutput(new IgnoreOutputStream());
            exec.setIgnoreExitValue(true);
        });
    }

    private static class IgnoreOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            //NOOP
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
        if (null == pythonDetails) {
            pythonDetails = getExtension().getDetails();
        }
        return pythonDetails;
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
