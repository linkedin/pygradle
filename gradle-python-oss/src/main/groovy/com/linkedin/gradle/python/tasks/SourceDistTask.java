package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.LiPythonComponent;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;


public class SourceDistTask extends DefaultTask {

  @TaskAction
  public void packageSdist() {

    final LiPythonComponent settings = getProject().getExtensions().getByType(LiPythonComponent.class);

    getProject().exec(new Action<ExecSpec>() {
      @Override
      public void execute(ExecSpec execSpec) {
        execSpec.environment(settings.pythonEnvironmentDistgradle);
        execSpec.commandLine(settings.pythonLocation, "'setup.py", "sdist");
      }
    });
  }

  @OutputFile
  public File getSdistOutput() {
    Project project = getProject();
    return new File(project.getBuildDir(), String.format("dist/%s-%s.tar.gz", project.getName(), project.getVersion()));
  }
}
