package com.linkedin.gradle.python.tasks;

import java.io.File;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;


public class InstallDependencies extends BasePythonTask {

  @OutputFile
  File getOutputFile() {
    return new File(getProject().getBuildDir(), getName() + ".txt");
  }

  @TaskAction
  public void doWork() {
    GFileUtils.writeFile("Hello", getOutputFile());
  }
}
