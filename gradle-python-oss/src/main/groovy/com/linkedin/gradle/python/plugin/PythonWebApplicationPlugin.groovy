package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.extension.DeployableExtension
import com.linkedin.gradle.python.extension.PexExtension
import com.linkedin.gradle.python.extension.WheelExtension
import com.linkedin.gradle.python.util.EntryPointHelpers
import com.linkedin.gradle.python.util.ExtensionUtils
import com.linkedin.gradle.python.util.PexFileUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PythonWebApplicationPlugin extends PythonBasePlugin {

    public final static String TASK_BUILD_PEX = 'buildPex'
    public final static String TASK_BUILD_WEB_APPLICATION = 'buildWebApplication'
    public final static String TASK_PACKAGE_WEB_APPLICATION = 'packageWebApplication'

    public final static String GUNICORN_ENTRYPOINT = 'gunicorn.app.wsgiapp:run'

    @Override
    void applyTo(Project project) {

        project.plugins.apply(PythonPexDistributionPlugin)

        DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project)
        WheelExtension wheelExtension = ExtensionUtils.maybeCreateWheelExtension(project)
        PexExtension pexExtension = ExtensionUtils.maybeCreatePexExtension(project)

        /**
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exploding gunicorn as a pex file.
         */
        project.tasks.create(TASK_BUILD_WEB_APPLICATION, BuildWebAppTask) { task ->
            task.dependsOn(TASK_BUILD_PEX)
            task.deployableExtension = deployableExtension
            task.wheelExtension = wheelExtension
            task.pexExtension = pexExtension
            task.pythonInterpreter = settings.details.virtualEnvInterpreter.path

        }

        def packageDeployable = project.tasks.create(TASK_PACKAGE_WEB_APPLICATION, Tar.class, new Action<Tar>() {
            @Override
            void execute(Tar tar) {
                tar.compression = Compression.GZIP
                tar.baseName = project.name
                tar.extension = 'tar.gz'
                tar.from(deployableExtension.deployableBuildDir)
            }
        })
        packageDeployable.dependsOn(project.tasks.getByName(TASK_BUILD_WEB_APPLICATION))

        project.artifacts.add(PythonPlugin.CONFIGURATION_DEFAULT, packageDeployable)

    }

    private static class BuildWebAppTask extends DefaultTask {

        DeployableExtension deployableExtension
        WheelExtension wheelExtension
        PexExtension pexExtension

        @Input
        String pythonInterpreter

        @InputDirectory
        public File getPexCache() {
            return pexExtension.pexCache
        }

        @InputDirectory
        public File getWheelCache() {
            return wheelExtension.wheelCache
        }

        @OutputFile
        public File getGunicornSource() {
            return new File(deployableExtension.deployableBinDir, "gunicorn")
        }

        @OutputDirectory
        public File getBinDir() {
            return deployableExtension.deployableBinDir
        }

        @TaskAction
        public void buildWebapp() {
            if (pexExtension.fatPex) {
                PexFileUtil.buildPexFile(project, getPexCache(), getGunicornSource().path, getWheelCache(), pythonInterpreter, GUNICORN_ENTRYPOINT)
            } else {
                EntryPointHelpers.writeEntryPointScript(project, project.file("${deployableExtension.deployableBinDir}/gunicorn").path, GUNICORN_ENTRYPOINT)
            }
        }
    }
}
