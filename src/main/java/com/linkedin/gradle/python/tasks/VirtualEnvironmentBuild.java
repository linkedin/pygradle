package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import java.io.File;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.util.VersionNumber;


public class VirtualEnvironmentBuild extends BasePythonTask {
  private Configuration virtualEnvFiles;

  @Inject
  protected FileOperations getFileOperations() {
    // Decoration takes care of the implementation
    throw new UnsupportedOperationException();
  }

  @OutputDirectory
  public File getVenvDir() {
    return venvDir;
  }

  @OutputFile
  public File getLocalPythonExecutable() {
    return new File(getVenvDir(), "bin/python");
  }

  @TaskAction
  public void doWork() {
    if(null == virtualEnvFiles) {
      throw new GradleException("Virtual Env must be defined");
    }

    final File vendorDir = new File(getPythonBuilDir(), "vendor");

    for (final File file : getVirtualEnvFiles()) {
      getFileOperations().copy(new Action<CopySpec>() {
        @Override
        public void execute(CopySpec copySpec) {
          copySpec.from(getFileOperations().tarTree(file));
          copySpec.into(vendorDir);
        }
      });
    }

    String virtualEnvDependencyVersion = findVirtualEnvDependencyVersion();
    final String path = String.format("%s/virtualenv-%s/virtualenv.py", vendorDir.getAbsolutePath(), virtualEnvDependencyVersion);
    final PythonExecutable pythonExecutable = getPythonToolChain().getPythonExecutable();

    pythonExecutable.execute(new Action<ExecAction>() {
      @Override
      public void execute(ExecAction execAction) {
        execAction.args(path, "--python", pythonExecutable.getFile().getAbsolutePath(), getVenvDir().getAbsolutePath());
      }
    }).assertNormalExitValue();
  }

  private String findVirtualEnvDependencyVersion() {
    ResolvedConfiguration resolvedConfiguration = getVirtualEnvFiles().getResolvedConfiguration();
    Set<ResolvedDependency> virtualEnvDependencies = resolvedConfiguration.getFirstLevelModuleDependencies(new VirtualEvnSpec());
    if(virtualEnvDependencies.isEmpty()) {
      throw new GradleException("Unable to find virtualenv dependency");
    }

    VersionNumber highest = new VersionNumber(0, 0, 0, null);
    for (ResolvedDependency resolvedDependency : virtualEnvDependencies) {
      VersionNumber test = VersionNumber.parse(resolvedDependency.getModuleVersion());
      if(test.compareTo(highest) > 0) {
        highest = test;
      }
    }

    return highest.toString();
  }

  @InputFiles
  Configuration getVirtualEnvFiles(){
    return virtualEnvFiles;
  }

  public void setVirtualEnvFiles(Configuration configuration) {
    this.virtualEnvFiles = configuration;
  }

  private class VirtualEvnSpec implements Spec<Dependency> {

    @Override
    public boolean isSatisfiedBy(Dependency element) {
      return "virtualenv".equals(element.getName());
    }
  }
}
