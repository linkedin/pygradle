package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.tasks.BuildWebAppTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;

import java.io.File;

public class PythonWebApplicationPlugin extends PythonBasePlugin {

    public static final String TASK_BUILD_PEX = "buildPex";
    public static final String TASK_BUILD_WEB_APPLICATION = "buildWebApplication";
    public static final String TASK_PACKAGE_WEB_APPLICATION = "packageWebApplication";
    public static final String GUNICORN_ENTRYPOINT = "gunicorn.app.wsgiapp:run";


    @Override
    public void applyTo(final Project project) {

        project.getPlugins().apply(PythonPexDistributionPlugin.class);

        final DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project);

        /*
         * Build a gunicorn pex file.
         *
         * Our apollo controllers expect to be able to "shell out" to a gunicorn
         * binary on disk that is next to the control file. Make sure this is
         * possible by exposing gunicorn as a pex file.
         */
        project.getTasks().create(TASK_BUILD_WEB_APPLICATION, BuildWebAppTask.class, task -> {
            task.setDescription("Build a web app, by default using gunicorn, but it's configurable.");
            task.dependsOn(TASK_BUILD_PEX);
            task.setExecutable(new File(deployableExtension.getDeployableBinDir(), "gunicorn"));
            task.setEntryPoint(GUNICORN_ENTRYPOINT);
        });

        // Make packaging task wait on this task so that gunicorn is packed into the app.
        project.getTasks().getByName(PythonPexDistributionPlugin.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.getTasks().getByName(TASK_BUILD_WEB_APPLICATION));

        /*
         * TODO: Remove this task once the backwards compatibility is not needed for it any more.
         */
        project.getTasks().create(TASK_PACKAGE_WEB_APPLICATION, task -> {
            task.setDescription("Backward compatibility place-holder task for packaging web app");
            task.dependsOn(project.getTasks().getByName(TASK_BUILD_WEB_APPLICATION));
            task.setEnabled(false);
        });

        project.getTasks().getByName(PythonPexDistributionPlugin.TASK_PACKAGE_DEPLOYABLE).dependsOn(project.getTasks().getByName(TASK_PACKAGE_WEB_APPLICATION));
    }
}
