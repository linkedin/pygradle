package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.LiPythonComponent
import com.linkedin.gradle.python.tasks.BuildWheelsTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PythonPexDistributionPlugin extends PythonBasePlugin {

  public final static String TASK_BUILD_WHEELS = 'buildWheels'
  public final static String TASK_BUILD_PEX = 'buildPex'
  public final static String TASK_PACKAGE_DEPLOYABLE = 'packageDeployable'

  @Override
  void applyTo(Project project) {

    project.plugins.apply(PythonPlugin)

    settings = project.getExtensions().getByType(LiPythonComponent)

    project.afterEvaluate {
      if (settings.pythonMajorMinor == '2.6') {
        project.dependencies.add(PythonPlugin.CONFIGURATION_BUILD_REQS,
            PythonPlugin.PINNED_VERSIONS['argparse'])
      }
    }
    project.dependencies.add(PythonPlugin.CONFIGURATION_BUILD_REQS,
        PythonPlugin.PINNED_VERSIONS['pex'])

    def pexSource = project.file("${settings.deployableBinDir}/${project.name}.pex").path

    // Recreate the pex cache if it exists so that we don't mistakenly use an old build's version of the local project.
    if (project.file(settings.pexCache).exists()) {
      project.file(settings.pexCache).deleteDir()
      project.file(settings.pexCache).mkdirs()
    }

    /**
     * Build wheels.
     *
     * We need wheels to build pex files.*/
    project.tasks.create(TASK_BUILD_WHEELS, BuildWheelsTask) {
      dependsOn project.tasks.getByName(PythonPlugin.TASK_INSTALL_PROJECT)
    }

    project.tasks.create(TASK_BUILD_PEX) {

      dependsOn(project.tasks.getByName(TASK_BUILD_WHEELS))

      doLast {
        project.exec {
          environment settings.pythonEnvironmentDistgradle
          commandLine([settings.pythonLocation,
                       settings.pipLocation,
                       'wheel',
                       '--disable-pip-version-check',
                       '--wheel-dir', settings.wheelCache,
                       '--no-deps',
                       '.'])
        }

        project.file(settings.deployableBuildDir).mkdirs()

        if (settings.fatPex) {
          // For each entry point, build a stand alone pex file
          collectEntryPoints(project).each {
            println "Processing entry point: ${it}"
            def (String name, String entry) = it.split('=')*.trim()
            buildPexFile(project,
                settings.pexCache,
                project.file("${settings.deployableBinDir}/${name}").path,
                settings.wheelCache,
                settings.interpreterPath,
                entry)
          }
        } else {
          // Build a single stand alone pex file
          buildPexFile(project,
              settings.pexCache,
              project.file(pexSource).path,
              settings.wheelCache,
              settings.interpreterPath,
              null)
          // For each entry point, write a thin wrapper
          collectEntryPoints(project).each {
            println "Processing entry point: ${it}"
            def (String name, String entry) = it.split('=')*.trim()
            writeEntryPointScript(project, project.file("${settings.deployableBinDir}/${name}").path, entry)
          }
        }
      }
    }

    def packageDeployable = project.tasks.create(TASK_PACKAGE_DEPLOYABLE, Tar.class, new Action<Tar>() {
      @Override
      void execute(Tar tar) {
        tar.compression = Compression.GZIP
        tar.baseName = project.name
        tar.extension = 'tar.gz'
        tar.from(settings.deployableBuildDir)
      }
    })
    packageDeployable.dependsOn(project.tasks.getByName(TASK_BUILD_PEX))

    project.artifacts.add(PythonPlugin.CONFIGURATION_DEFAULT, packageDeployable)
  }
}
