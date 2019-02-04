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

package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;


public interface ContainerExtension {
    public static final String TASK_BUILD_PROJECT_WHEEL = "buildProjectWheel";
    public static final String TASK_BUILD_WHEELS = "buildWheels";
    public static final String TASK_PACKAGE_DEPLOYABLE = "packageDeployable";
    public static final String TASK_BUILD_CONTAINERS = "buildContainers";

    /**
     * Prepare the extension by adding dependencies and doing any other necessary initializations.
     */
    public void prepareExtension(Project project);

    /**
     * Create any additional tasks the extension needs.  Such tasks must
     * implement the PythonContainerTask interface.  All tasks implementing
     * this interface will automatically be inserted into the task dependency
     * graph.  Implementers of this interface should *not* explicitly add
     * their tasks to the graph.
     */
    public void makeTasks(Project project);
}
