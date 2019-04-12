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
package com.linkedin.gradle.python.util.entrypoint;

import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.extension.ZipappContainerExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import groovy.text.SimpleTemplateEngine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public class EntryPointWriter {

    private final String template;
    private final boolean isCliTool;
    private final boolean isZipapp;

    public EntryPointWriter(Project project, String template) {
        this.template = template;

        this.isCliTool = ExtensionUtils.findPythonComponentExtension(project, CliExtension.class) != null;
        this.isZipapp = ExtensionUtils.findPythonComponentExtension(project, ZipappContainerExtension.class) != null;
    }

    public void writeEntryPoint(File location, Map<String, String> properties) throws IOException, ClassNotFoundException {
        if (location.exists()) {
            location.delete();
        }

        location.createNewFile();

        SimpleTemplateEngine simpleTemplateEngine = new SimpleTemplateEngine();
        String rendered = simpleTemplateEngine.createTemplate(template).make(properties).toString();
        FileUtils.write(location, rendered);

        if (isCliTool || isZipapp) {
            location.setExecutable(true, false);
            location.setReadable(true, false);
        } else {
            location.setExecutable(true);
        }
    }
}
