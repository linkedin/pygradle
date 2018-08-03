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
package com.linkedin.gradle.python.tasks.provides;

import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import org.gradle.api.Task;

/**
 * Marker interface for tasks that make a Venv.
 *
 * This is used so that we can track venv's and the Python ABI they support
 */
public interface ProvidesVenv extends Task {
    /**
     * The PythonAbiContainer
     */
    void setEditablePythonAbiContainer(EditablePythonAbiContainer editablePythonAbiContainer);
}
