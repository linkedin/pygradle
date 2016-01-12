package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.utilities.PipDependencyInstallAction;
import com.linkedin.gradle.python.tasks.internal.utilities.PipInstallHelper;
import com.linkedin.gradle.python.tasks.internal.utilities.TaskUtils;
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


@ParallelizableTask
public class InstallDependenciesTask extends BasePythonTask {

  private Configuration dependencyConfiguration;

  @TaskAction
  public void installDependencies() {
    PipInstallHelper pipInstallHelper = new PipInstallHelper(getPythonEnvironment().getPythonExecutable(), new PipDependencyInstallAction(getVenvDir()));
    preformFullInstall(pipInstallHelper);
  }

  private void preformFullInstall(PipInstallHelper pipInstallHelper) {
    for (final File dependency : getDependencyConfiguration()) {
      pipInstallHelper.install(dependency);
    }
  }

  @OutputDirectories
  public Set<File> getDependencies() {
    Set<File> insalledSitePackages = createFileDir(dependencyConfiguration.getDependencies());
    getLogger().info("Packages dir: {}", insalledSitePackages);
    return insalledSitePackages;
  }

  private Set<File> createFileDir(DependencySet dependencies) {
    HashSet<File> sitePackages = new HashSet<File>();
    for (Dependency dependency : dependencies) {
      sitePackages.add(new File(TaskUtils.sitePackage(getVenvDir(), pythonEnvironment.getVersion()), dependency.getName()));
    }
    return sitePackages;
  }

  @InputFiles
  public Configuration getDependencyConfiguration() {
    return dependencyConfiguration;
  }

  public void setDependencyConfiguration(Configuration configuration) {
    this.dependencyConfiguration = configuration;
  }
}
