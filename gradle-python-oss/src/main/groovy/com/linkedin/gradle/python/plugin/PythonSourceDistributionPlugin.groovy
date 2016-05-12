package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.tasks.SourceDistTask
import org.gradle.api.Project

class PythonSourceDistributionPlugin extends PythonBasePlugin {

    public final static String TASK_PACKAGE_SDIST = 'packageSdist'

    @Override
    void applyTo(Project project) {

        /**
         * Create a Python source distribution.
         */
        def sdistPackageTask = project.tasks.create(TASK_PACKAGE_SDIST, SourceDistTask) {
            dependsOn(project.tasks.getByName(PythonPlugin.TASK_INSTALL_PROJECT))
        }

        def sdistArtifactInfo = [
                name: project.name,
                type: 'tgz',
                extension: 'tar.gz',
                file: sdistPackageTask.getSdistOutput(),
                builtBy: project.tasks.getByName(TASK_PACKAGE_SDIST),
        ]

        project.artifacts.add(PythonPlugin.CONFIGURATION_DEFAULT, sdistArtifactInfo)

    }

}
