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
package com.linkedin.gradle.python.util.internal.zipapp;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.zipapp.EntryPointTemplateProvider;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThinZipappGenerator implements ZipappGenerator {

    protected static Logger logger = Logging.getLogger(ThinZipappGenerator.class);

    protected final Project project;
    protected final List<String> options;
    protected final EntryPointTemplateProvider templateProvider;
    protected final Map<String, String> extraProperties;

    public ThinZipappGenerator(
        Project project,
        List<String> options,
        EntryPointTemplateProvider templateProvider,
        Map<String, String> extraProperties) {

        this.project = project;
        this.options = options;
        this.templateProvider = templateProvider;
        this.extraProperties = extraProperties == null ? new HashMap<>() : extraProperties;
    }

    @Override
    public Map<String, String> buildSubstitutions(PythonExtension extension, String entry) {
            Map<String, String> substitutions = new HashMap<>();
            substitutions.putAll(extraProperties);
            substitutions.put("entryPoint", entry);
            substitutions.put("pythonExecutable", extension.getDetails().getSystemPythonInterpreter().getAbsolutePath());
            substitutions.put("toolName", project.getName());
            return substitutions;
    }

    @Override
    public void buildEntryPoints() throws Exception {
        // Generic zipapps don't build anything, so subclasses should override
        // this to build their entry point specific artifacts.
    };
}
