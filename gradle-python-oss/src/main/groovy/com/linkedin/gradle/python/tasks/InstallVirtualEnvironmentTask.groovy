/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.tasks

import com.linkedin.gradle.python.PythonExtension
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

@CompileStatic
class InstallVirtualEnvironmentTask extends DefaultTask {

  PythonExtension component

  public PythonExtension getComponent() {
    if (component == null) {
      component = getProject().getExtensions().getByType(PythonExtension.class);
    }
    return component;
  }

  @InputFiles
  Configuration getPyGradleBootstrap() {
    project.configurations.getByName('pygradleBootstrap')
  }

  @OutputFile
  File getVirtualEnvDir() {
    return getComponent().getDetails().getVirtualEnvInterpreter()
  }

  @TaskAction
  @CompileDynamic
  void installVEnv() {

    File packageDir = new File(project.buildDir, "virtualenv-dir")
    if (packageDir.exists()) {
      project.delete(packageDir)
    }

    packageDir.mkdirs()

    getPyGradleBootstrap().files.each { file ->

      project.copy {
        from project.tarTree(file.path)
        into packageDir
      }
    }

    def version = findVirtualEnvDependencyVersion()
    project.exec {
      commandLine(
              getComponent().getDetails().getSystemPythonInterpreter(),
              project.file("${packageDir}/virtualenv-${version}/virtualenv.py"),
              '--python', getComponent().getDetails().getSystemPythonInterpreter(),
              '--prompt', getComponent().getDetails().virtualEnvPrompt,
              getComponent().getDetails().getVirtualEnv()
      )
    }

    project.delete(packageDir)
  }

  private String findVirtualEnvDependencyVersion() {
    ResolvedConfiguration resolvedConfiguration = getPyGradleBootstrap().getResolvedConfiguration();
    Set<ResolvedDependency> virtualEnvDependencies = resolvedConfiguration.getFirstLevelModuleDependencies(new VirtualEnvSpec());
    if (virtualEnvDependencies.isEmpty()) {
      throw new GradleException("Unable to find virtualenv dependency");
    }

    VersionNumber highest = new VersionNumber(0, 0, 0, null);
    for (ResolvedDependency resolvedDependency : virtualEnvDependencies) {
      VersionNumber test = VersionNumber.parse(resolvedDependency.getModuleVersion());
      if (test.compareTo(highest) > 0) {
        highest = test;
      }
    }

    return highest.toString();
  }

  private class VirtualEnvSpec implements Spec<Dependency> {

    @Override
    public boolean isSatisfiedBy(Dependency element) {
      return "virtualenv" == element.getName();
    }
  }
}
