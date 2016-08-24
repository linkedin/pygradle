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

package com.linkedin.gradle.python.plugin.internal.sources;

import com.linkedin.gradle.python.spec.binary.internal.SourceDistBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.configuration.SourceDistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.util.GUtil;


/**
 * This plugin will automatically add source dists to all {@link PythonComponentSpecInternal}'s.
 * <p>
 * This is done by first creating a binary with {@link #createSourceDistBinaries(ModelMap, PythonComponentSpecInternal)},
 * then using {@link #createSourceDistTask(ModelMap, SourceDistBinarySpecInternal)} to create the actual task the make source dist(s).
 */
public class SourceDistRulePlugin extends RuleSource {

    @BinaryTasks
    public void createSourceDistTask(ModelMap<Task> tasks, final SourceDistBinarySpecInternal spec) {
        String postFix = GUtil.toCamelCase(spec.getName());
        String taskName = "create" + postFix;
        tasks.create(taskName, BuildSourceDistTask.class, new SourceDistConfigurationAction(spec));
    }
}
