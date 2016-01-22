package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.GFileUtils;


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
