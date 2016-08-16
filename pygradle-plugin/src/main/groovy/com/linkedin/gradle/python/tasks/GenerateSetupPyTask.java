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
package com.linkedin.gradle.python.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class GenerateSetupPyTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(GenerateSetupPyTask.class);

    public GenerateSetupPyTask() {
        setDescription("Writes the suggested setup.py out to disk. This will overwrite any existing setup.py");
        setGroup("documentation");
    }

    @TaskAction
    public void createSetupPy() throws IOException {
        File file = getProject().file("setup.py");
        if (file.exists()) {
            logger.lifecycle("Contents of setup.py are going to be overwritten!!");
            file.delete();
        }
        file.createNewFile();

        String setupPy = IOUtils.toString(GenerateSetupPyTask.class.getResourceAsStream("/templates/setup.py.template"));
        FileUtils.write(file, setupPy);
    }
}
