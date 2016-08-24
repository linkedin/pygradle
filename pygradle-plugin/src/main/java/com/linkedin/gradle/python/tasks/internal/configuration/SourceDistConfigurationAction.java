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

import com.linkedin.gradle.python.spec.binary.internal.SourceDistBinarySpecInternal;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;


public class SourceDistConfigurationAction extends DistConfigurationAction<BuildSourceDistTask> {

    private final SourceDistBinarySpecInternal spec;

    public SourceDistConfigurationAction(SourceDistBinarySpecInternal spec) {
        super(spec.getPythonEnvironment(), spec.getSources());
        this.spec = spec;
    }

    @Override
    public void doConfigure(BuildSourceDistTask task) {
        task.setOutputFormat(spec.getArtifactType());
    }
}
