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
import com.linkedin.gradle.python.extension.MypyExtension;
import com.linkedin.gradle.python.extension.CoverageExtension;
import com.linkedin.gradle.python.tasks.AbstractPythonMainSourceDefaultTask;
import com.linkedin.gradle.python.tasks.AbstractPythonTestSourceDefaultTask;
import com.linkedin.gradle.python.tasks.CheckStyleGeneratorTask;
import com.linkedin.gradle.python.tasks.Flake8Task;
import com.linkedin.gradle.python.tasks.MypyTask;
import com.linkedin.gradle.python.tasks.PyCoverageTask;
import com.linkedin.gradle.python.tasks.PyTestTask;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static com.linkedin.gradle.python.util.StandardTextValues.TASK_CHECK;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_CHECKSTYLE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_COVERAGE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_FLAKE;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_INSTALL_PROJECT;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_MYPY;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_PYTEST;

public class ValidationPlugin implements Plugin<Project> {
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

        /*
         * Run tests using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        project.getTasks().create(TASK_PYTEST.getValue(), PyTestTask.class,
            task -> task.onlyIf(it -> project.file(settings.testDir).exists()));

        // Add a dependency to task ``check`` to depend on our Python plugin's ``pytest`` task
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_PYTEST.getValue()));

        /*
         * Run coverage using py.test.
         *
         * This uses the ``setup.cfg`` if present to configure py.test.
         */
        CoverageExtension cov = ExtensionUtils.maybeCreate(project, "coverage", CoverageExtension.class);
        project.getTasks().create(TASK_COVERAGE.getValue(), PyCoverageTask.class,
            task -> task.onlyIf(it -> project.file(settings.testDir).exists() &&
                                /* The test suite and other environments run
                                 * the coverage task explicitly, so check
                                 * whether the flag is set *or* its been
                                 * explicitly invoked.
                                 */
                                (cov.isRun() || project.getGradle().getStartParameter().getTaskNames().contains("coverage"))
                                ));

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
         * This uses the mypy.ini file if present to configure mypy.
         */
        MypyExtension mypy = ExtensionUtils.maybeCreate(project, "mypy", MypyExtension.class);
        project.getTasks().create(TASK_MYPY.getValue(), MypyTask.class,
            task -> task.onlyIf(it -> project.file(settings.srcDir).exists() && mypy.isRun()));

        // Make task "check" depend on mypy task.
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_MYPY.getValue()));

        /*
         * Create checkstyle styled report from flake
         */
        project.getTasks().create(TASK_CHECKSTYLE.getValue(), CheckStyleGeneratorTask.class);

        // Add a dependency to task ``check`` to depend on our Python plugin's ``flake8`` task
        project.getTasks().getByName(TASK_CHECK.getValue())
            .dependsOn(project.getTasks().getByName(TASK_FLAKE.getValue()));
    }

}
