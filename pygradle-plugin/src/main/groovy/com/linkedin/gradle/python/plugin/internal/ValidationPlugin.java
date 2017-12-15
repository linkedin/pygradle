package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.CheckStyleGeneratorTask;
import com.linkedin.gradle.python.tasks.Flake8Task;
import com.linkedin.gradle.python.tasks.PyCoverageTask;
import com.linkedin.gradle.python.tasks.PyTestTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ValidationPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {

        PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        /*
         * Run tests using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        project.getTasks().create(StandardTextValues.TASK_PYTEST.getValue(), PyTestTask.class,
            task -> task.onlyIf(it -> project.file(settings.testDir).exists()));

        // Add a dependency to task ``check`` to depend on our Python plugin's ``pytest`` task
        project.getTasks().getByName(StandardTextValues.TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(StandardTextValues.TASK_PYTEST.getValue()));

        /*
         * Run coverage using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        project.getTasks().create(StandardTextValues.TASK_COVERAGE.getValue(), PyCoverageTask.class,
            task -> task.onlyIf(it -> project.file(settings.testDir).exists()));

        /*
         * Run flake8.
         *
         * This uses the ``setup.cfg`` if present to configure flake8.
         */
        project.getTasks().create(StandardTextValues.TASK_FLAKE.getValue(), Flake8Task.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() || project.file(settings.testDir).exists()));

        /*
         * Create checkstyle styled report from flake
         */
        project.getTasks().create(StandardTextValues.TASK_CHECKSTYLE.getValue(), CheckStyleGeneratorTask.class);

        // Add a dependency to task ``check`` to depend on our Python plugin's ``flake8`` task
        project.getTasks().getByName(StandardTextValues.TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(StandardTextValues.TASK_FLAKE.getValue()));
    }

}
