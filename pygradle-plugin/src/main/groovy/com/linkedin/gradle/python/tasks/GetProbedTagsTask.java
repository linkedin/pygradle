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

import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.action.ProbeVenvInfoAction;
import com.linkedin.gradle.python.tasks.provides.ProvidesVenv;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;


/**
 * Get supported wheel tags from previously probed virtual environment.
 *
 * <p>When virtual environment creation task is up-to-date,
 * then ABI container does not get populated, remains empty, and
 * no cached wheels can match any tags. This task ensures that
 * the container does get populated, regardless of virtual environment
 * creation task outcome.</p>
 */
public class GetProbedTagsTask extends DefaultTask implements ProvidesVenv {
    private PythonDetails pythonDetails;
    private EditablePythonAbiContainer editablePythonAbiContainer;

    @TaskAction
    public void getProbedTags() {
        ProbeVenvInfoAction.getProbedTags(getProject(), pythonDetails, editablePythonAbiContainer);
    }

    @Override
    public void setEditablePythonAbiContainer(EditablePythonAbiContainer editablePythonAbiContainer) {
        this.editablePythonAbiContainer = editablePythonAbiContainer;
    }

    public PythonDetails getPythonDetails() {
        return pythonDetails;
    }

    public void setPythonDetails(PythonDetails pythonDetails) {
        this.pythonDetails = pythonDetails;
    }
}
