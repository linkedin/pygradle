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
package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.plugin.internal.DocumentationPlugin;
import com.linkedin.gradle.python.plugin.internal.InstallDependenciesPlugin;
import com.linkedin.gradle.python.plugin.internal.ValidationPlugin;
import com.linkedin.gradle.python.tasks.CleanSaveVenvTask;
import com.linkedin.gradle.python.tasks.GenerateSetupPyTask;
import com.linkedin.gradle.python.tasks.GetProbedTagsTask;
import com.linkedin.gradle.python.tasks.InstallVirtualEnvironmentTask;
import com.linkedin.gradle.python.tasks.PinRequirementsTask;
import com.linkedin.gradle.python.tasks.provides.ProvidesVenv;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageFiltering;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings;
import com.linkedin.gradle.python.util.DefaultPackageSettings;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.FileSystemUtils;
import com.linkedin.gradle.python.util.internal.PyPiRepoUtil;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import com.linkedin.gradle.python.wheel.internal.DefaultPythonAbiContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.BasePlugin;

import java.io.File;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_BOOTSTRAP_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_BUILD_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYDOCS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_PYTHON;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_SETUP_REQS;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_TEST;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_VENV;
import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_WHEEL;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_CLEAN_SAVE_VENV;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_GET_PROBED_TAGS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_PIN_REQUIREMENTS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_SETUP_LINKS;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_SETUP_PY_WRITER;
import static com.linkedin.gradle.python.util.StandardTextValues.TASK_VENV_CREATE;

public class PythonPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {

        final PythonExtension settings = project.getExtensions().create("python", PythonExtension.class, project);

        EditablePythonAbiContainer supportedWheelFormats =
            ExtensionUtils.maybeCreate(settings, EditablePythonAbiContainer.class.getName(), DefaultPythonAbiContainer.class);

        project.getTasks().withType(ProvidesVenv.class, it -> it.setEditablePythonAbiContainer(supportedWheelFormats));

        project.getPlugins().apply("base");

        createConfigurations(project);
        configureVendedDependencies(project, settings);

        project.getTasks().withType(SupportsPackageFiltering.class, action -> {
            action.getOutputs().doNotCacheIf("When package packageFilter is set",
                task -> ((SupportsPackageFiltering) task).getPackageExcludeFilter() != null);
        });

        DefaultPackageSettings packageSettings = new DefaultPackageSettings(project.getProjectDir());
        project.getTasks().withType(SupportsPackageInfoSettings.class, it -> it.setPackageSettings(packageSettings));

        /*
         * To prevent base dependencies, such as setuptools, from installing/reinstalling, we will
         * pin their versions to values in the extension.forcedVersions map, which will contain known
         * good versions that satisfy all the requirements.
         */
        final PyGradleDependencyResolveDetails dependencyResolveDetails = new PyGradleDependencyResolveDetails(settings.forcedVersions);
        project.getConfigurations()
            .forEach(configuration -> configuration.getResolutionStrategy().eachDependency(dependencyResolveDetails));

        /*
         * Write the direct dependencies into a requirements file as a list of pinned versions.
         */
        final PinRequirementsTask pinRequirementsTask = project.getTasks().create(TASK_PIN_REQUIREMENTS.getValue(), PinRequirementsTask.class);

        /*
         * Install virtualenv.
         *
         * Install the virtualenv version that we implicitly depend on so that we
         * can run on systems that don't have virtualenv already installed.
         */
        project.getTasks().create(TASK_VENV_CREATE.getValue(), InstallVirtualEnvironmentTask.class, task -> {
            task.dependsOn(pinRequirementsTask);
            task.setPythonDetails(settings.getDetails());
        });
        // Ensure that ABI container is populated when virtual environment creation task is up-to-date.
        project.getTasks().create(TASK_GET_PROBED_TAGS.getValue(), GetProbedTagsTask.class, task -> {
            task.dependsOn(TASK_VENV_CREATE.getValue());
            task.setPythonDetails(settings.getDetails());
        });

        /*
         * Creates a link so users can activate into the virtual environment.
         */
        project.getTasks().create(TASK_SETUP_LINKS.getValue(), task -> {
            task.dependsOn(project.getTasks().getByName(TASK_VENV_CREATE.getValue()));
            task.dependsOn(project.getTasks().getByName(TASK_GET_PROBED_TAGS.getValue()));
            task.getOutputs().file(settings.getDetails().getActivateLink());

            task.doLast(it -> {
                File activateLinkSource = settings.getDetails().getVirtualEnvironment().getScript("activate");
                File activateLink = settings.getDetails().getActivateLink();
                FileSystemUtils.makeSymLinkUnchecked(activateLinkSource, activateLink);
            });
        });

        /*
         * task that cleans the project but leaves the venv intact.  Helpful for projects on windows that
         * take a very long time to build the venv.
         */
        project.getTasks().create(TASK_CLEAN_SAVE_VENV.getValue(), CleanSaveVenvTask.class,
            task -> task.setGroup(BasePlugin.BUILD_GROUP));

        project.getTasks().create(TASK_SETUP_PY_WRITER.getValue(), GenerateSetupPyTask.class);

        project.getPlugins().apply(InstallDependenciesPlugin.class);
        project.getPlugins().apply(ValidationPlugin.class);
        project.getPlugins().apply(DocumentationPlugin.class);

        project.getPlugins().apply(LayeredWheelCachePlugin.class);

        PyPiRepoUtil.setupPyGradleRepo(project);
    }

    private static void createConfigurations(Project project) {
        Configuration pythonConf = project.getConfigurations().create(CONFIGURATION_PYTHON.getValue());
        /*
         * To resolve transitive dependencies, we need the 'default' configuration
         * to extend the 'python' configuration. This is because the source
         * configuration must match the configuration which the artifact is
         * published to (i.e., 'default' in our case).
         */
        project.getConfigurations().getByName("default").extendsFrom(pythonConf);

        project.getConfigurations().create(CONFIGURATION_BOOTSTRAP_REQS.getValue());
        project.getConfigurations().create(CONFIGURATION_SETUP_REQS.getValue());
        Configuration buildReq = project.getConfigurations().create(CONFIGURATION_BUILD_REQS.getValue());
        project.getConfigurations().create(CONFIGURATION_PYDOCS.getValue());
        project.getConfigurations().create(CONFIGURATION_TEST.getValue());
        project.getConfigurations().create(CONFIGURATION_VENV.getValue());
        project.getConfigurations().create(CONFIGURATION_WHEEL.getValue());
        // TODO: Kept for backwards compatibility. Remove when not needed (very soon).
        // Even though flake8 is no longer in its own virtual env, users may still be adding libraries through the
        // flake8 configuration, which still needs to be added to the build config so those libraries exist in the venv.
        Configuration flake8 = project.getConfigurations().create("flake8");
        buildReq.extendsFrom(flake8);
    }

    /*
     * Add vended build and test dependencies to projects that apply this plugin.
     * Notice that virtualenv contains the latest versions of setuptools,
     * pip, and wheel, vended in. Make sure to use versions we can actually
     * use based on various restrictions. For example, pex may limit the
     * highest version of setuptools used. Provide the dependencies in the
     * best order they should be installed in setupRequires configuration.
     */
    private static void configureVendedDependencies(Project project, PythonExtension settings) {
        project.getDependencies().add(CONFIGURATION_BOOTSTRAP_REQS.getValue(), settings.forcedVersions.get("virtualenv"));

        project.getDependencies().add(CONFIGURATION_SETUP_REQS.getValue(), settings.forcedVersions.get("setuptools"));
        project.getDependencies().add(CONFIGURATION_SETUP_REQS.getValue(), settings.forcedVersions.get("wheel"));
        project.getDependencies().add(CONFIGURATION_SETUP_REQS.getValue(), settings.forcedVersions.get("pip"));
        project.getDependencies().add(CONFIGURATION_SETUP_REQS.getValue(), settings.forcedVersions.get("setuptools-git"));

        project.getDependencies().add(CONFIGURATION_BUILD_REQS.getValue(), settings.forcedVersions.get("flake8"));

        project.getDependencies().add(CONFIGURATION_BUILD_REQS.getValue(), settings.forcedVersions.get("Sphinx"));

        project.getDependencies().add(CONFIGURATION_TEST.getValue(), settings.forcedVersions.get("pytest"));
        project.getDependencies().add(CONFIGURATION_TEST.getValue(), settings.forcedVersions.get("pytest-cov"));
        project.getDependencies().add(CONFIGURATION_TEST.getValue(), settings.forcedVersions.get("pytest-xdist"));
    }
}
