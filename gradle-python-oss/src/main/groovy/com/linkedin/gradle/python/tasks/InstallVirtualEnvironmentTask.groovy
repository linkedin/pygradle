package com.linkedin.gradle.python.tasks


import com.linkedin.gradle.python.LiPythonComponent
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CompileStatic
class InstallVirtualEnvironmentTask extends DefaultTask {

  LiPythonComponent component

  public LiPythonComponent getComponent() {
    if (component == null) {
      component = getProject().getExtensions().getByType(LiPythonComponent.class);
    }
    return component;
  }

  @InputFiles
  Configuration getPyGradleBootstrap() {
    project.configurations.getByName('pygradleBootstrap')
  }

  @OutputDirectory
  File getPackageDir() {
    return project.file(getComponent().virtualenvPackageDir)
  }

  @TaskAction
  @CompileDynamic
  void installVEnv() {

    File packageDir = getPackageDir()
    packageDir.mkdirs()

    getPyGradleBootstrap().files.each { file ->

      project.copy {
        from project.tarTree(file.path)
        into packageDir
      }
    }
  }
}
