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
package com.linkedin.gradle.python.util.internal.pex;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.util.EntryPointHelpers;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.PexFileUtil;
import com.linkedin.gradle.python.util.entrypoint.EntryPointWriter;
import com.linkedin.gradle.python.util.internal.zipapp.DefaultTemplateProviderOptions;
import com.linkedin.gradle.python.util.internal.zipapp.ThinZipappGenerator;
import com.linkedin.gradle.python.util.pip.PipFreezeAction;
import com.linkedin.gradle.python.util.zipapp.EntryPointTemplateProvider;
import org.gradle.api.Project;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.List;
import java.util.Map;


public class ThinPexGenerator extends ThinZipappGenerator {

    public ThinPexGenerator(
        Project project,
        List<String> pexOptions,
        EntryPointTemplateProvider templateProvider,
        Map<String, String> extraProperties) {

        super(project, pexOptions, templateProvider, extraProperties);
        logger = Logging.getLogger(ThinPexGenerator.class);
    }

    @Override
    public Map<String, String> buildSubstitutions(PythonExtension extension, String entry) {
        Map<String, String> substitutions = super.buildSubstitutions(extension, entry);
        substitutions.put("realPex", PexFileUtil.createThinPexFilename(project.getName()));
        return substitutions;
    }

    @Override
    public void buildEntryPoints() throws Exception {
        PythonExtension extension = ExtensionUtils.getPythonExtension(project);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(extension, PexExtension.class);
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(
            extension, DeployableExtension.class);

        Map<String, String> dependencies = new PipFreezeAction(project).getDependencies();

        PexExecSpecAction action = PexExecSpecAction.withOutEntryPoint(
            project, project.getName(), options, dependencies);

        ExecResult exec = project.exec(action);
        new PexExecOutputParser(action, exec).validatePexBuildSuccessfully();

        for (String it : EntryPointHelpers.collectEntryPoints(project)) {
            logger.lifecycle("Processing entry point: {}", it);
            String[] split = it.split("=");
            String name = split[0].trim();
            String entry = split[1].trim();
            Map<String, String> substitutions = buildSubstitutions(extension, entry);

            DefaultTemplateProviderOptions providerOptions = new DefaultTemplateProviderOptions(project, extension, entry);
            new EntryPointWriter(
                project,
                templateProvider.retrieveTemplate(providerOptions, pexExtension.isPythonWrapper()))
                .writeEntryPoint(new File(deployableExtension.getDeployableBinDir(), name), substitutions);
        }
    }
}
