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
package com.linkedin.gradle.python.extension;

import com.linkedin.gradle.python.extension.internal.DefaultPythonDetails;
import com.linkedin.gradle.python.util.OperatingSystem;
import org.gradle.api.Project;

import java.io.File;

/**
 * Intended to dispense Python Details given a project.
 */
public class PythonDetailsFactory {
    private PythonDetailsFactory() {
        //Private constructor for utility class
    }

    /**
     * Make a new PythonDetails
     */
    public static PythonDetails makePythonDetails(Project project, File venv) {
        return new DefaultPythonDetails(project, venv);
    }

    /**
     * Make a new PythonDetails
     */
    public static PythonDetails withNewVenv(Project project, PythonDetails fromDetails, File venv) {
        DefaultPythonDetails nextDetails = new DefaultPythonDetails(project, venv);
        nextDetails.setPythonInterpreter(fromDetails.getPythonVersion(), fromDetails.getSystemPythonInterpreter());
        return nextDetails;
    }

    /**
     * @return The name of the "exec" dir
     */
    public static String getPythonApplicationDirectory() {
        return OperatingSystem.current().isWindows() ? "Scripts" : "bin";
    }
}
