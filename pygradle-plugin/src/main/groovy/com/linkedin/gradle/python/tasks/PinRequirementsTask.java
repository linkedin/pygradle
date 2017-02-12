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

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class PinRequirementsTask extends DefaultTask {

    private static final Logger logger = Logging.getLogger(PinRequirementsTask.class);

    @TaskAction
    public void writeOutPinnedFile() throws IOException {
        File pinnedFile = getPinnedFile();
        if (pinnedFile.exists()) {
            pinnedFile.delete();
        }
        pinnedFile.createNewFile();

        // Only add "soft pins" for the *direct* dependencies, so that we don't
        // burn in assumptions about our transitive dependency tree to the output
        // source artifact.
        StringBuilder contents = new StringBuilder();
        for (ResolvedDependency r : getPythonConfiguration().getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            logger.info("Pinning {}=={}", r.getModuleName(), r.getModuleVersion());
            contents.append(r.getModuleName())
                .append("==")
                .append(r.getModuleVersion())
                .append(System.getProperty("line.separator"));  //moduleName==moduleVersion\n
        }

        FileUtils.write(pinnedFile, contents);
    }

    @InputFiles
    public Configuration getPythonConfiguration() {
        return getProject()
            .getConfigurations()
            .getByName(StandardTextValues.CONFIGURATION_PYTHON.getValue());
    }

    @OutputFile
    public File getPinnedFile() {
        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(getProject());
        return pythonExtension.getPinnedFile();
    }
}
