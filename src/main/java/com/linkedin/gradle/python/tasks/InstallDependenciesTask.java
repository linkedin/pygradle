package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.PipDependencyInstallAction;
import com.linkedin.gradle.python.tasks.internal.TaskUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.util.GFileUtils;

import java.io.File;


public class InstallDependenciesTask extends BasePythonTask {

  private Configuration virtualEnvFiles;
  private File installDir;
  private PipDependencyInstallAction action;

  @TaskAction
  public void doWork() {
    final PythonExecutable pythonExecutable = getPythonToolChain().getLocalPythonExecutable(venvDir);

    for (final File dependency : getVirtualEnvFiles()) {
      ExecResult execute = pythonExecutable.execute(getAction().install(dependency));
      if(execute.getExitValue() != 0) {
        getLogger().lifecycle(getAction().getWholeText());
        execute.assertNormalExitValue();
      }
    }

    GFileUtils.writeFile(installDir.getAbsolutePath(), getPthFile());
  }

  @OutputDirectory
  File getInstallDir() {
    return installDir;
  }

  @OutputFile
  public File getPthFile() {
    return new File(TaskUtils.sitePackage(venvDir, getPythonVersion()), String.format("%s.pth", getName()));
  }

  @InputFiles
  Configuration getVirtualEnvFiles(){
    return virtualEnvFiles;
  }

  public PipDependencyInstallAction getAction() {
    if(action == null) {
      action = new PipDependencyInstallAction(venvDir, getInstallDir());
    }
    return action;
  }

  public void setVirtualEnvFiles(Configuration configuration) {
    this.virtualEnvFiles = configuration;
  }

  public void setInstallDir(File installDir) {
    this.installDir = installDir;
  }
}
