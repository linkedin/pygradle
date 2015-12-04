package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import com.linkedin.gradle.python.tasks.internal.InstallDependenciesIncrementalAction;
import com.linkedin.gradle.python.tasks.internal.PipDependencyInstallAction;
import com.linkedin.gradle.python.tasks.internal.PipInstallHelper;
import com.linkedin.gradle.python.tasks.internal.TaskUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectories;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.process.ExecResult;


@ParallelizableTask
public class InstallDependenciesTask extends BasePythonTask {

  private Configuration virtualEnvFiles;

  @TaskAction
  public void installDependencies(IncrementalTaskInputs inputs) {
    final PythonExecutable pythonExecutable = getPythonToolChain().getLocalPythonExecutable(venvDir);
    PipInstallHelper pipInstallHelper = new PipInstallHelper(pythonExecutable, new PipDependencyInstallAction(venvDir));

    if (inputs.isIncremental()) {
      InstallDependenciesIncrementalAction incrementalAction = new InstallDependenciesIncrementalAction(pipInstallHelper);
      inputs.outOfDate(incrementalAction);
      inputs.removed(incrementalAction);
    } else {
      preformFullInstall(pipInstallHelper);
    }
  }

  private void preformFullInstall(PipInstallHelper pipInstallHelper) {
    for (final File dependency : getVirtualEnvFiles()) {
      pipInstallHelper.install(dependency);
    }
  }

  @OutputDirectories
  public Set<File> getDependencies() {
    Set<File> insalledSitePackages = createFileDir(virtualEnvFiles.getDependencies());
    getLogger().info("Packages dir: {}", insalledSitePackages);
    return insalledSitePackages;
  }

  private Set<File> createFileDir(DependencySet dependencies) {
    HashSet<File> sitePackages = new HashSet<File>();
    for (Dependency dependency : dependencies) {
      sitePackages.add(new File(TaskUtils.sitePackage(venvDir, pythonToolChain.getVersion()), dependency.getName()));
    }
    return sitePackages;
  }

  @InputFiles
  public Configuration getVirtualEnvFiles() {
    return virtualEnvFiles;
  }

  public void setVirtualEnvFiles(Configuration configuration) {
    this.virtualEnvFiles = configuration;
  }
}
