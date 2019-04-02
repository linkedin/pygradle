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


public class NoopBuildPexTask extends DefaultTask implements PythonContainerTask {
    private static final Logger LOG = Logging.getLogger(NoopBuildPexTask.class);
    private static final String DISABLED_MESSAGE =
          "######################### WARNING ##########################\n"
        + "The buildPex task has been deprecated.\n"
        + "Please use the assemblerContainers task instead.\n"
        + "############################################################";

    public Task dependsOn(Object... paths) {
        LOG.warn(DISABLED_MESSAGE);
        return super.dependsOn(paths);
    }
}
