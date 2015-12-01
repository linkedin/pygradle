package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.PipDependencyInstallAction;
import com.linkedin.gradle.python.tasks.internal.TaskUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


@ParallelizableTask
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
  }

  @OutputDirectories
  Set<File> dependencies() {
    HashSet<File> files = new HashSet<File>();
    for (String pckg : action.getPackages()) {
      File e = new File(TaskUtils.sitePackage(venvDir, pythonToolChain.getVersion()), pckg);
      getLogger().lifecycle("Dependency Directory: {}", e.getAbsolutePath());
      files.add(e);
    }
    return files;
  }

  @OutputDirectory
  File getInstallDir() {
    return installDir;
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
