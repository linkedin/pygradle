package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.tasks.BasePythonTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.GFileUtils;


abstract public class AbstractDistTask extends BasePythonTask {

    private final String setupPyCommand;
    protected final List<String> args = new ArrayList<String>();

    @Input
    public File distributablePath = new File(getProject().getBuildDir(), "distributable");

    private List<File> outputFiles = new ArrayList<File>();

    @InputFiles
    public List<File> sourceSet = new ArrayList<File>();

    protected AbstractDistTask(String setupPyCommand, String... args) {
        this.setupPyCommand = setupPyCommand;
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    public List<String> extraArgs() {
      return Collections.emptyList();
    }

    @TaskAction
    public void buildSourceDist() {
        final File tempFolder = new File(getPythonEnvironment().getPythonBuildDir(), "temp_distributable");

        args.addAll(extraArgs());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExecResult sdist = execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.setIgnoreExitValue(true);
                execAction.setStandardOutput(outputStream);
                execAction.setErrorOutput(outputStream);
                execAction.args("setup.py", setupPyCommand, "--dist-dir", tempFolder.getAbsolutePath());
                execAction.args(args);
            }
        });

        if(sdist.getExitValue() != 0) {
            getLogger().lifecycle("Building Source Dist failed\n{}", outputStream.toString());
        }
        sdist.assertNormalExitValue();

        for (File file : getProject().fileTree(tempFolder).getFiles()) {
            File destination = new File(distributablePath, file.getName());
            if(destination.exists()) {
                GFileUtils.deleteQuietly(destination);
            }
            GFileUtils.moveFile(file, destination);
            outputFiles.add(destination);
        }
    }

    @OutputFiles
    List<File> getOuptutFiles() {
      return outputFiles;
    }

    public void sourceSet(SourceDirectorySet source) {
        sourceSet.addAll(source.getSrcDirs());
    }
}
