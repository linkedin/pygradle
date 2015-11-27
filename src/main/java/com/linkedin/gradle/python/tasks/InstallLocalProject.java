package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import org.gradle.api.Action;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.GFileUtils;

import java.io.File;


public class InstallLocalProject extends BasePythonTask {

  @TaskAction
  public void doWork() {
    final PythonExecutable pythonExecutable = getPythonToolChain().getLocalPythonExecutable(venvDir);
    final String pipCommand = new File(venvDir, "bin/pip").getAbsolutePath();
    StringBuilder stringBuilder = new StringBuilder();

    pythonExecutable.execute(new Action<ExecAction>() {
        @Override
        public void execute(ExecAction execAction) {
          execAction.args(pipCommand, "install", "--editable", getProject().getProjectDir().getAbsolutePath());
        }
      });


    GFileUtils.writeFile(stringBuilder.toString(), getInstalledDependencies());
  }

  @OutputFile
  File getInstalledDependencies() {
    return new File(getPythonBuilDir(), getName() + ".txt");
  }

  @InputFile
  File getSetupPyFile() {
    return new File(getProject().getProjectDir(), "setup.py");
  }

}
