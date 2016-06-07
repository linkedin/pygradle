package com.linkedin.gradle.python.plugin

import com.linkedin.gradle.python.util.EntryPointHelpers
import org.gradle.api.Action
import org.gradle.api.Project
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

        def gunicornSource = project.file("${settings.deployableBinDir}/gunicorn").path

        /**
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exploding gunicorn as a pex file.
         */
        project.tasks.create(TASK_BUILD_WEB_APPLICATION) {
            dependsOn(TASK_BUILD_PEX)
            outputs.dir(settings.deployableBinDir)
            outputs.file(gunicornSource)
            doLast {
                if (settings.fatPex) {
                    buildPexFile(project, settings.pexCache, gunicornSource, settings.wheelCache, settings.interpreterPath, GUNICORN_ENTRYPOINT)
                } else {
                    EntryPointHelpers.writeEntryPointScript(project, project.file("${settings.deployableBinDir}/gunicorn").path, GUNICORN_ENTRYPOINT)
                }
            }
        }

        def packageDeployable = project.tasks.create(TASK_PACKAGE_WEB_APPLICATION, Tar.class, new Action<Tar>() {
            @Override
            void execute(Tar tar) {
                tar.compression = Compression.GZIP
                tar.baseName = project.name
                tar.extension = 'tar.gz'
                tar.from(settings.deployableBuildDir)
            }
        })
        packageDeployable.dependsOn(project.tasks.getByName(TASK_BUILD_WEB_APPLICATION))

        project.artifacts.add(PythonPlugin.CONFIGURATION_DEFAULT, packageDeployable)

    }

}
