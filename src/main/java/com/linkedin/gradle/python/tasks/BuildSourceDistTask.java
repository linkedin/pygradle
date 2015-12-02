package com.linkedin.gradle.python.tasks;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildSourceDistTask extends BasePythonTask {

    List<File> outputFiles = new ArrayList<File>();

    @InputFiles
    List<File> sourceSet = new ArrayList<File>();

    @TaskAction
    public void doWork() {
        final File distributable = new File(getProject().getBuildDir(), "distributable");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult sdist = getPythonToolChain().getPythonExecutable().execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.setIgnoreExitValue(true);
                execAction.setStandardOutput(outputStream);
                execAction.setErrorOutput(outputStream);
                execAction.args("setup.py", "sdist", "--dist-dir", distributable.getAbsolutePath(), "--formats=gztar,zip");
            }
        });

        if(sdist.getExitValue() != 0) {
            getLogger().lifecycle("Building Source Dist failed\n{}", outputStream.toString());
        }
        sdist.assertNormalExitValue();

        outputFiles.addAll(getProject().fileTree(distributable).getFiles());
    }

    @OutputFiles
    List<File> getOuptutFiles(){
        return outputFiles;
    }

    public void sourceSet(SourceDirectorySet source) {
        sourceSet.addAll(source.getSrcDirs());
    }
}
