package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.FinalPythonArtifact;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuildSourceDistTask extends BasePythonTask implements FinalPythonArtifact {

    @Input
    public File distributablePath = new File(getProject().getBuildDir(), "distributable");

    public List<File> outputFiles = new ArrayList<File>();

    @InputFiles
    public List<File> sourceSet = new ArrayList<File>();

    @TaskAction
    public void buildSourceDist() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ExecResult sdist = execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.setIgnoreExitValue(true);
                execAction.setStandardOutput(outputStream);
                execAction.setErrorOutput(outputStream);
                execAction.args("setup.py", "sdist", "--dist-dir", distributablePath.getAbsolutePath(), "--formats=gztar,zip");
            }
        });

        if(sdist.getExitValue() != 0) {
            getLogger().lifecycle("Building Source Dist failed\n{}", outputStream.toString());
        }
        sdist.assertNormalExitValue();

        outputFiles.addAll(getProject().fileTree(distributablePath).getFiles());
    }

    @OutputFiles
    List<File> getOuptutFiles(){
        return outputFiles;
    }

    public void sourceSet(SourceDirectorySet source) {
        sourceSet.addAll(source.getSrcDirs());
    }
}
