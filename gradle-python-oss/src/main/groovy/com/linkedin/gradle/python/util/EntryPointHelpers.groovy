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
package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.PythonExtension
import org.gradle.api.Project

public class EntryPointHelpers {

    private EntryPointHelpers() {
        //private constructor for util class
    }

    /**
     * Run ``setup.py entrypoints`` and return the results.
     *
     * The ``entrypoints`` setuptools command is a custom command provided by the distgradle.GradleDistribution
     * distribution class. Only classes that use this distribution class support this command.
     *
     * @param project The project to run ``setup.py entrypoints`` within.
     * @return A list of setuptools entry point strings that looks like ['foo = module1.module2:main', ...].
     */
    static public List<String> collectEntryPoints(Project project) {
        PythonExtension settings = project.getExtensions().getByType(PythonExtension)
        def entryPointsBuf = new ByteArrayOutputStream()
        project.exec {
            environment settings.pythonEnvironment + settings.pythonEnvironmentDistgradle
            commandLine([
                    VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                    'setup.py',
                    'entrypoints',
            ])
            standardOutput entryPointsBuf
        }
        def entryPoints = []
        entryPointsBuf.toString().split('\n').each {
            if (it != 'running entrypoints') {
                entryPoints << it
            }
        }
        return entryPoints
    }
}
