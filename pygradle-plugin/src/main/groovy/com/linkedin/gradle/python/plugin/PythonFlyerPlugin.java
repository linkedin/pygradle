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

import com.linkedin.gradle.python.util.ApplicationContainer;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.FileSystemUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.nio.file.Paths;

/**
 * A Flyer plugin.
 * <p>
 * A Flyer project is a deployable project that consumes the resources
 * from an Ember subproject.
 * <p>
 * On top of li-python-deployable, this plugin will setup the dependencies
 * on the resource files from the Ember subproject. To make it easier to
 * access the resources, we will create a link under the Python project, that
 * connects to the resources folder under the Ember project. Since it's a link,
 * developers don't have to rebuild the Python project each time they modify
 * the Ember code.
 * <p>
 * For the deployment, the plugin will copy the resources into the 'deployable'
 * folder under the path {@code 'build/<project name>/'} that LID could find all the
 * resources there.
 * <p>
 * <pre>
 * <code>
 * apply plugin: 'python-flyer'
 *
 * dependencies {
 *   ...
 *   resource project(path: ':example-flask-ember-fe', configuration: 'emberStaticContent')
 * }
 * </code>
 * </pre>
 */
public class PythonFlyerPlugin implements Plugin<Project> {

    public static final String TASK_SETUP_RESOURCE_LINK = "setupResourceLink";
    public static final String TASK_PACKAGE_RESOURCE_FILES = "packageResourceFiles";

    @Override
    public void apply(final Project project) {

        project.getPlugins().apply(PythonWebApplicationPlugin.class);

        /*
         * This configuration is used to connect the Python project to the Ember project.
         * We will use this configuration to access the static resource, create the symlink and package the resource.
         */
        final Configuration resourceConf = project.getConfigurations().create("resource");

        /*
         * Under the Python project, we need to make a link to the Ember resource(<ember-project>/dist for now).
         * By doing this, it becomes easier for the Python project to access the resource files.
         * And for both local development and LID deployment, the resource folder will have the
         * same relative directory. This will also simplify the logic in python project.
         */
        project.getTasks().create(TASK_SETUP_RESOURCE_LINK, task -> {
            task.dependsOn(resourceConf);
            task.doLast(it -> {
                if (!Paths.get(project.getProjectDir().getAbsolutePath(), "resource").toFile().exists()) {
                    project.getLogger().lifecycle("Making the Symlink: {}{}resource --> {}",
                        project.getProjectDir(), File.separatorChar, String.valueOf(resourceConf.getSingleFile()));
                    FileSystemUtils.makeSymLinkUnchecked(resourceConf.getSingleFile(), new File(project.getProjectDir(), "resource"));
                }
            });
        });

        project.getTasks().getByName(StandardTextValues.TASK_INSTALL_PROJECT.getValue())
            .dependsOn(project.getTasks().getByName(TASK_SETUP_RESOURCE_LINK));

        /*
         * In order to make the resource files accessible when deploying the project, we need to copy the
         * static files into the 'deployable' directory.
         */
        project.getTasks().create(TASK_PACKAGE_RESOURCE_FILES, Copy.class, copy -> {
            final DeployableExtension deployableExtension = ExtensionUtils.maybeCreateDeployableExtension(project);
            copy.dependsOn(project.getTasks().getByName(PythonWebApplicationPlugin.TASK_BUILD_WEB_APPLICATION));

            copy.from(resourceConf);
            copy.into(deployableExtension.getDeployableBuildDir().toPath().resolve("resource"));
        });

        // Make sure we've copied all the files before running the task: packageDeployable
        project.getTasks().getByName(ApplicationContainer.TASK_PACKAGE_DEPLOYABLE)
            .dependsOn(project.getTasks().getByName(TASK_PACKAGE_RESOURCE_FILES));
    }
}
