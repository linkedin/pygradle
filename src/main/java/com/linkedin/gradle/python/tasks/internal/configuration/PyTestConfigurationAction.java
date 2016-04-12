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

import com.linkedin.gradle.python.PythonTestSourceSet;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class PyTestConfigurationAction extends BasePythonTaskAction<PythonTestTask> {

    private final Set<File> testSourceDirs = new HashSet<File>();

    public PyTestConfigurationAction(PythonEnvironment pythonEnvironment, ModelMap<LanguageSourceSet> testDir) {
        super(pythonEnvironment);
        for (PythonTestSourceSet pythonTestSourceSet : testDir.withType(PythonTestSourceSet.class)) {
            testSourceDirs.addAll(pythonTestSourceSet.getSource().getSrcDirs());
        }
    }

    @Override
    public void configure(PythonTestTask task) {
        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
        task.registerTestSources(testSourceDirs);
        task.setOutputFile(new File(getPythonEnvironment().getBuildDir(),
                "test-results" + File.separatorChar + getPythonEnvironment().getEnvironmentName() + "-" + task.getName() + ".xml"));
    }
}
