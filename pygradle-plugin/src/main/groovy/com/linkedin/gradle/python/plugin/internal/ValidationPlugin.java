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
package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.BlackExtension;
import com.linkedin.gradle.python.extension.IsortExtension;
import com.linkedin.gradle.python.extension.MypyExtension;
import com.linkedin.gradle.python.extension.CoverageExtension;
import com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask;
import com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask;
import com.linkedin.gradle.python.tasks.BlackTask;
import com.linkedin.gradle.python.tasks.CheckStyleGeneratorTask;
import com.linkedin.gradle.python.tasks.Flake8Task;
import com.linkedin.gradle.python.tasks.MypyTask;
import com.linkedin.gradle.python.tasks.IsortTask;
import com.linkedin.gradle.python.tasks.PyCoverageTask;
import com.linkedin.gradle.python.tasks.PyTestTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import java.util.function.BiPredicate;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import static com.linkedin.gradle.python.util.StandardTextValues.TASK_BLACK;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_CHECK;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_CHECKSTYLE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_COVERAGE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_FLAKE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_PROJECT;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_MYPY;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_ISORT;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_PYTEST;

public class ValidationPlugin implements Plugin<Project> {
    private final static Logger LOG = Logging.getLogger(ValidationPlugin.class);

    @Override
    public void apply(final Project project) {

        PythonExtension settings = ExtensionUtils.getPythonExtension(project);

        /*
         * Any task that extends from {@link AbstractPythonMainSourceDefaultTask} will require TASK_INSTALL_BUILD_REQS
         */
        project.getTasks().withType(AbstractPythonMainSourceDefaultTask.class,
            task -> task.dependsOn(project.getTasks().getByName(TASK_INSTALL_BUILD_REQS.getValue())));

        /*
         * Any task that extends from {@link AbstractPythonTestSourceDefaultTask} will require TASK_INSTALL_PROJECT
         */
        project.getTasks().withType(AbstractPythonTestSourceDefaultTask.class,
            task -> task.dependsOn(project.getTasks().getByName(TASK_INSTALL_PROJECT.getValue())));

        CoverageExtension cov = ExtensionUtils.maybeCreate(project, TASK_COVERAGE.getValue(), CoverageExtension.class);

        BiPredicate<String, Boolean> pytestOrCoverage = (taskName, covShouldBeEnabled) ->
            project.file(settings.testDir).exists() && (cov.isRun() == covShouldBeEnabled
                // Task should be run if it is explicitly invoked.
                || project.getGradle().getStartParameter().getTaskNames().contains(taskName));
        /*
         * Run tests using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         * Pytest task will not be run if coverage is set to true, unless explicitly executed. If coverage is enabled,
         * the coverage task will execute the test suite.
         */
        project.getTasks().create(TASK_PYTEST.getValue(), PyTestTask.class,
            task -> task.onlyIf(it -> {
                boolean shouldRun = pytestOrCoverage.test(TASK_PYTEST.getValue(), false);
                if (!shouldRun) {
                    LOG.info("Skipping pytest task; Either you don't have tests written or coverage is enabled and "
                        + "pytests will be run during the coverage task instead.");
                }
                return shouldRun;
            }));

        // Add a dependency to task ``check`` to depend on our Python plugin's ``pytest`` task
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_PYTEST.getValue()));

        /*
         * Run coverage using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */

        project.getTasks().create(TASK_COVERAGE.getValue(), PyCoverageTask.class,
            task -> task.onlyIf(it -> pytestOrCoverage.test(TASK_COVERAGE.getValue(), true)));

        // Make task "check" depend on coverage task.
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_COVERAGE.getValue()));

        /*
         * Run flake8.
         *
         * This uses the ``setup.cfg`` if present to configure flake8.
         */
        project.getTasks().create(TASK_FLAKE.getValue(), Flake8Task.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() || project.file(settings.testDir).exists()));

        /*
         * Run mypy.
         *
         * This uses the setup.cfg (or mypy.ini) file if present to configure mypy.
         */
        MypyExtension mypy = ExtensionUtils.maybeCreate(project, "mypy", MypyExtension.class);
        project.getTasks().create(TASK_MYPY.getValue(), MypyTask.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() && mypy.isRun()));

        // Make task "check" depend on mypy task.
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_MYPY.getValue()));

        /*
         * Run isort.
         */
        IsortExtension isort = ExtensionUtils.maybeCreate(project, "isort", IsortExtension.class);
        project.getTasks().create(TASK_ISORT.getValue(), IsortTask.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() && isort.isRun()));

        // Make task "check" depend on isort task.
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_ISORT.getValue()));

        /*
         * Run black.
         */
        BlackExtension black = ExtensionUtils.maybeCreate(project, "black", BlackExtension.class);
        project.getTasks().create(TASK_BLACK.getValue(), BlackTask.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() && black.isRun()));

        // Make task "check" depend on black task.
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_BLACK.getValue()));

        /*
         * Create checkstyle styled report from flake
         */
        project.getTasks().create(TASK_CHECKSTYLE.getValue(), CheckStyleGeneratorTask.class);

        // Add a dependency to task ``check`` to depend on our Python plugin's ``flake8`` task
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_FLAKE.getValue()));
    }

}
