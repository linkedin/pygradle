package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask;
import com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask;
import com.linkedin.gradle.python.tasks.PipInstallTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

public class InstallDependenciesPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        final PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        /*
         * Install core setup requirements into virtualenv.
         *
         * Core setup requirements are the packages we need to run Python build for this product.
         * They need to be installed in a specific order. Hence, we set sorted to false here.
         */
        project.getTasks().create(StandardTextValues.TASK_INSTALL_SETUP_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_SETUP_LINKS.getValue()));
            it.setArgs(Collections.singletonList("--upgrade"));
            it.setInstallFileCollection(project.getConfigurations().getByName("setupRequires"));
            it.setSorted(false);
        });

        /*
         * Install build requirements into virtualenv.
         *
         * The build requirements are the packages we need to run all stages of the build for this product.
         */
        project.getTasks().create(StandardTextValues.TASK_INSTALL_BUILD_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_SETUP_REQS.getValue()));
            it.setArgs(Collections.singletonList("--upgrade"));
            it.setInstallFileCollection(project.getConfigurations().getByName("build"));
        });

        /*
         * Install the product's Python test requirements.
         *
         * A products test requirements are those that are listed in the ``test`` configuration and are only required for running tests.
         */
        project.getTasks().create(StandardTextValues.TASK_INSTALL_TEST_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_BUILD_REQS.getValue()));
            it.setInstallFileCollection(project.getConfigurations().getByName("test"));
        });

        /*
         * Install the product's Python requirements.
         *
         * A products Python requirements are those listed in the python configuration.
         *
         */
        project.getTasks().create(StandardTextValues.TASK_INSTALL_PYTHON_REQS.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            // by running after test reqs we ensure, in case different versions of same req exist in both, that version from python reqs takes precedence.
            it.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_TEST_REQS.getValue()));
            it.setInstallFileCollection(project.getConfigurations().getByName("python"));
        });

        /*
         * Install the product itself.
         *
         * This installs the product itself in editable mode. It is equivalent to running ``python setup.py develop`` on a Python product.
         */
        project.getTasks().create(StandardTextValues.TASK_INSTALL_PROJECT.getValue(), PipInstallTask.class, it -> {
            it.setPythonDetails(settings.getDetails());
            it.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PYTHON_REQS.getValue()));
            it.setInstallFileCollection(project.files(project.file(project.getProjectDir())));
            it.setArgs(Collections.singletonList("--editable"));
            it.setEnvironment(settings.pythonEnvironmentDistgradle);
        });

        /*
         * Any task that extends from {@link AbstractPythonMainSourceDefaultTask} will require TASK_INSTALL_BUILD_REQS
         */
        project.getTasks().withType(AbstractPythonMainSourceDefaultTask.class,
            task -> task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_BUILD_REQS.getValue())));

        /*
         * Any task that extends from {@link AbstractPythonTestSourceDefaultTask} will require TASK_INSTALL_PROJECT
         */
        project.getTasks().withType(AbstractPythonTestSourceDefaultTask.class,
            task -> task.dependsOn(project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue())));
    }

}
