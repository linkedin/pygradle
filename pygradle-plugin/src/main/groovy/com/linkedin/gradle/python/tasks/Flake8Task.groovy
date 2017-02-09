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
package com.linkedin.gradle.python.tasks

import groovy.transform.CompileStatic
import org.gradle.process.ExecResult

@CompileStatic
public class Flake8Task extends AbstractPythonMainSourceDefaultTask {

    public void preExecution() {
        /*
         Modified to only include folders that exist. if no folders exist, then
         the task isn't actually run.
          */
        def sArgs = [pythonDetails.virtualEnvironment.findExecutable("flake8").absolutePath,
                     "--config", "$component.setupCfg"]

        def paths = []
        if (project.file(component.srcDir).exists()) {
            paths.add(component.srcDir)
        }

        if (project.file(component.testDir).exists()) {
            paths.add(component.testDir)
        }

        if (paths.size() > 0) {
            sArgs.addAll(paths)
            args(sArgs.toListString())
        } else {
            project.logger.lifecycle("Flake8 task skipped, no folders")
        }
    }

    @Override
    void processResults(ExecResult execResult) {
    }
}
