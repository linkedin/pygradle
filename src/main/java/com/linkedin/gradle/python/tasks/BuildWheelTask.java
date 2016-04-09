package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;

import java.io.File;
import java.util.Collections;
import java.util.List;


public class BuildWheelTask extends AbstractDistTask {
    public BuildWheelTask() {
        super("bdist_wheel");
    }

    public List<String> extraArgs() {
        return Collections.singletonList(String.format("--python-tag=py%s", getPythonVersion().getVersionString().replace(".", "")));
    }

    @Override
    public String getExtension() {
        return "whl";
    }

    @Override
    //TODO: This is bad, but since pip can't tell us the name of the artifact before it's built, it must be done :-(
    protected File getPythonArtifact() {
        return new File(distributablePath,
                String.format("%s-%s-py%s-%s.%s", getProject().getName(), getProject().getVersion(),
                        getPythonEnvironment().getVersion().getMajorMinorVersion().replace(".", ""),
                        System.getProperty("os.name"),
                        getExtension()));
    }

}
