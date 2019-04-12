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
import com.linkedin.gradle.python.extension.ZipappContainerExtension;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PexFileUtil;
import com.linkedin.gradle.python.util.entrypoint.EntryPointWriter;
import com.linkedin.gradle.python.util.internal.pex.FatPexGenerator;
import com.linkedin.gradle.python.util.internal.zipapp.DefaultTemplateProviderOptions;
import com.linkedin.gradle.python.util.zipapp.DefaultWebappEntryPointTemplateProvider;
import com.linkedin.gradle.python.util.zipapp.EntryPointTemplateProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BuildWebAppTask extends DefaultTask {

    private File executable;
    private String entryPoint;
    private List<String> pexOptions = new ArrayList<>();
    private EntryPointTemplateProvider templateProvider = new DefaultWebappEntryPointTemplateProvider();

    @Input
    @Optional
    public List<String> getPexOptions() {
        return pexOptions;
    }

    public void setPexOptions(List<String> pexOptions) {
        this.pexOptions = pexOptions;
    }

    @OutputFile
    public File getExecutable() {
        return executable;
    }

    @Input
    public String getEntryPoint() {
        return entryPoint;
    }

    @TaskAction
    public void buildWebapp() throws IOException, ClassNotFoundException {
        Project project = getProject();
        PythonExtension pythonExtension = ExtensionUtils.getPythonExtension(project);
        ZipappContainerExtension zipappExtension = ExtensionUtils.getPythonComponentExtension(
            project, ZipappContainerExtension.class);

        // Regardless of whether fat or thin zipapps are used, the container
        // plugin will build the right container (i.e. .pex or .pyz).
        // However, for thin zipapps, we need additional wrapper scripts
        // generated (e.g. the gunicorn wrapper).
        if (zipappExtension.isFat()) {
            // 2019-04-11(warsaw): FIXME: For now, we're still hard coding pex
            // for the gunicorn file.
            new FatPexGenerator(project, pexOptions).buildEntryPoint(
                PexFileUtil.createFatPexFilename(executable.getName()), entryPoint, null);
        } else {
            HashMap<String, String> substitutions = new HashMap<>();
            substitutions.put("entryPoint", entryPoint);
            substitutions.put("realPex", PexFileUtil.createThinPexFilename(project.getName()));
            substitutions.put("toolName", project.getName());
            String template = templateProvider.retrieveTemplate(
                // Use the shell wrapper for web applications.
                new DefaultTemplateProviderOptions(project, pythonExtension, entryPoint),
                false);
            new EntryPointWriter(project, template).writeEntryPoint(executable, substitutions);
        }
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Input
    @Optional
    public EntryPointTemplateProvider getTemplateProvider() {
        return templateProvider;
    }

    public void setTemplateProvider(EntryPointTemplateProvider templateProvider) {
        this.templateProvider = templateProvider;
    }
}
