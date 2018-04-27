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
package com.linkedin.gradle.python.util.pip;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.util.OperatingSystem;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Map;

public class PipConfFile {

    private Project project;
    private PythonDetails pythonDetails;
    private String fileExtension;

    public PipConfFile(Project project, PythonDetails pythonDetails) {
        this.project = project;
        this.pythonDetails = pythonDetails;

        if (OperatingSystem.current().isWindows()) {
            fileExtension = "ini";
        } else {
            fileExtension = "conf";
        }

    }

    /**
     * Writes a new pip.conf file.  You can configure the contents of this file with the python extension
     * or leave it blank for it to pick up the pip.conf in your system properties.
     * <p>
     * index-url = https://pypi.org/simple/
     */
    public void buildPipConfFile() throws IOException {

        File pip = Paths.get(pythonDetails.getVirtualEnv().getAbsolutePath(), "pip." + fileExtension).toFile().getAbsoluteFile();
        if (!pip.exists()) {
            project.getLogger().info("creating pip." + fileExtension);
            pip.createNewFile();

            PythonExtension pythonExtension = project.getExtensions().getByType(PythonExtension.class);

            Map<String, Map<String, String>> pipConfig = pythonExtension.pipConfig;

            if (pipConfig.size() > 0) {
                PrintWriter writer = new PrintWriter(pip, "UTF-8");

                for (Map.Entry<String, Map<String, String>> entry : pipConfig.entrySet()) {
                    writer.println("[" + entry.getKey() + "]");

                    for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                        writer.println(entry2.getKey() + " = " + entry2.getValue());
                    }
                }

                writer.close();
            }
        }
    }
}
