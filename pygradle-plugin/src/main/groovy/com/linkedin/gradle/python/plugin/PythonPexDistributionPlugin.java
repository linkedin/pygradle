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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import org.gradle.api.Project;


public class PythonPexDistributionPlugin extends PythonContainerPlugin {
    @Override
    public void applyTo(final Project project) {
        final PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);

        // Even though this is the default, explicit is better than implicit.
        pythonExtension.setContainer("pex");
        super.applyTo(project);
    }
}
