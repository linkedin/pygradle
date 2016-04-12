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

package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;


abstract public class DistConfigurationAction<T extends AbstractDistTask> extends BasePythonTaskAction<T> {
    private final ModelMap<LanguageSourceSet> sourceSets;

    public DistConfigurationAction(PythonEnvironment pythonEnvironment, ModelMap<LanguageSourceSet> sourceSets) {
        super(pythonEnvironment);
        this.sourceSets = sourceSets;
    }

    @Override
    public void configure(T task) {
        for (PythonSourceSet pythonSourceSet : sourceSets.withType(PythonSourceSet.class)) {
            task.sourceSet(pythonSourceSet.getSource());
        }

        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
        task.shouldRunAfter("check");
        doConfigure(task);
    }

    abstract public void doConfigure(T task);
}
