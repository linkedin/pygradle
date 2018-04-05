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
import com.linkedin.gradle.python.util.zipapp.TemplateProviderOptions;
import org.gradle.api.Project;

/**
 * Provides some values to help pick which template to use
 */
public class DefaultTemplateProviderOptions implements TemplateProviderOptions {
    private final Project project;
    private final PythonExtension extension;
    private final String entryPoint;

    public DefaultTemplateProviderOptions(Project project, PythonExtension extension, String entryPoint) {
        this.project = project;
        this.extension = extension;
        this.entryPoint = entryPoint;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public PythonExtension getExtension() {
        return extension;
    }

    @Override
    public String getEntryPoint() {
        return entryPoint;
    }
}
