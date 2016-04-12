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

package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import org.gradle.api.Action;


public class WheelAction implements Action<WheelBinarySpec> {
    private final PythonEnvironment pythonEnvironment;

    public WheelAction(PythonEnvironment pythonEnvironment) {
        this.pythonEnvironment = pythonEnvironment;
    }

    @Override
    public void execute(WheelBinarySpec wheelBinarySpec) {
        wheelBinarySpec.setPythonEnvironment(pythonEnvironment);
    }
}
