package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.utilities.PipLocalInstallAction;
import com.linkedin.gradle.python.tasks.internal.utilities.TaskUtils;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;

import java.io.File;


public class InstallLocalProjectTask extends BasePythonTask {

  @TaskAction
  public void installLocalProject() {
    final PythonExecutable pythonExecutable = getPythonToolChain().getLocalPythonExecutable(venvDir);
    PipLocalInstallAction pipLocalInstallAction = new PipLocalInstallAction(venvDir);
    ExecResult execute = pythonExecutable.execute(pipLocalInstallAction.install(getProject().getProjectDir()));
    if(execute.getExitValue() != 0) {
      getLogger().lifecycle(pipLocalInstallAction.getWholeText());
    }
    execute.assertNormalExitValue();
  }

  @OutputFile
  public File getEggLink() {
    return new File(TaskUtils.sitePackage(venvDir, getPythonVersion()), String.format("%s.egg-link", getProject().getName()));
  }

  @InputFile
  public File getSetupPyFile() {
    return new File(getProject().getProjectDir(), "setup.py");
  }

}
