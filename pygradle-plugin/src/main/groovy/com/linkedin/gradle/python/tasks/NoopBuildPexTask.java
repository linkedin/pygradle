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
package com.linkedin.gradle.python.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;


public class NoopBuildPexTask extends DefaultTask implements PythonContainerTask, NoopTask {
    private static final Logger LOG = Logging.getLogger(NoopBuildPexTask.class);
    private static final String DISABLED_MESSAGE =
          "######################### WARNING ##########################\n"
        + "The buildPex task has been deprecated.\n"
        + "Please use the assembleContainers task instead.\n"
        + "############################################################";

    // This is used to suppress the warning when PythonContainerPlugin plumbs
    // this task into the task hierarchy, which isn't user code.
    private boolean suppressWarning = false;

    @TaskAction
    public void noOp() { }

    public Task dependsOn(Object... paths) {
        if (!suppressWarning) {
            LOG.warn(DISABLED_MESSAGE);
        }
        return super.dependsOn(paths);
    }

    public boolean suppressWarning() {
        return suppressWarning;
    }

    public void setSuppressWarning(boolean suppressWarning) {
        this.suppressWarning = suppressWarning;
    }
}
