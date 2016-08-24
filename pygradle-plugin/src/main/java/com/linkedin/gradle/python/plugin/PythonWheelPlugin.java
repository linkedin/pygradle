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

import com.linkedin.gradle.python.plugin.internal.wheel.WheelAction;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.PythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentBinaries;


public class PythonWheelPlugin implements Plugin<Project> {
    public void apply(final Project project) {
        project.getPluginManager().apply(PythonLangPlugin.class);
        project.getPluginManager().apply(Rules.class);
    }

    public static class Rules extends RuleSource {
        @ComponentBinaries
        public void createWheelBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
            final PythonEnvironmentContainer container = spec.getPythonEnvironments();

            for (PythonEnvironment pythonEnvironment : container.getPythonEnvironments().values()) {
                String name = spec.getName() + "Wheel" + pythonEnvironment.getVersion().getVersionString();
                if (!binarySpecs.containsKey(name)) {
                    binarySpecs.create(name, WheelBinarySpec.class, new WheelAction(pythonEnvironment));
                }
            }
        }
    }
}
