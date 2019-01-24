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

package com.linkedin.gradle.python.util;

import org.gradle.api.Project;


public interface ApplicationContainer {
    public static final String TASK_BUILD_PROJECT_WHEEL = "buildProjectWheel";
    public static final String TASK_BUILD_WHEELS = "buildWheels";
    public static final String TASK_PACKAGE_DEPLOYABLE = "packageDeployable";
    public static final String TASK_ASSEMBLE_CONTAINERS = "assembleContainers";

    /**
     * Add any extensions that your container exposes.  This runs when the
     * plugin is applied.
     */
    public void addExtensions(Project project);

    /**
     * Add any additional dependencies your application container format
     * needs.  This runs after the project is evaluated.
     */
    public void addDependencies(Project project);

    /**
     * Create any additional tasks the extension needs.  Such tasks must
     * implement the PythonContainerTask interface.  All tasks implementing
     * this interface will automatically be inserted into the task dependency
     * graph.  Implementers of this interface should *not* explicitly add
     * their tasks to the graph.  This runs after the project is evaluated.
     */
    public void makeTasks(Project project);
}
