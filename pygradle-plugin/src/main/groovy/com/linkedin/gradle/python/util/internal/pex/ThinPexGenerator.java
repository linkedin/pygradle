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

import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.util.EntryPointHelpers;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.entrypoint.EntryPointWriter;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThinPexGenerator implements PexGenerator {

    private static final Logger logger = Logging.getLogger(ThinPexGenerator.class);

    private final Project project;
    private final String entryPointTemplate;
    private final Map<String, String> extraProperties;

    public ThinPexGenerator(Project project, String entryPointTemplate, Map<String, String> extraProperties) {
        this.project = project;
        this.entryPointTemplate = entryPointTemplate;
        this.extraProperties = extraProperties == null ? new HashMap<>() : extraProperties;
    }

    @Override
    public void buildEntryPoints() throws Exception {
        DeployableExtension deployableExtension = ExtensionUtils.getPythonComponentExtension(project, DeployableExtension.class);
        PexExtension pexExtension = ExtensionUtils.getPythonComponentExtension(project, PexExtension.class);

        List<String> dependencies = new PipFreezeAction(project).getDependencies();

        PexExecSpecAction action = PexExecSpecAction.withOutEntryPoint(project, project.getName(), dependencies);

        ExecResult exec = project.exec(action);
        new PexExecOutputParser(action, exec).validatePexBuildSuccessfully();

        for (String it : EntryPointHelpers.collectEntryPoints(project)) {
            logger.lifecycle("Processing entry point: {}", it);
            String[] split = it.split("=");
            String name = split[0].trim();
            String entry = split[1].trim();

            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.putAll(extraProperties);
            propertyMap.put("realPex", project.getName() + ".pex");
            propertyMap.put("entryPoint", entry);

            new EntryPointWriter(project, getTemplate(pexExtension))
                .writeEntryPoint(new File(deployableExtension.getDeployableBinDir(), name), propertyMap);
        }
    }

    private String getTemplate(PexExtension pexExtension) throws IOException {
        if (entryPointTemplate != null) {
            return entryPointTemplate;
        }

        if (ExtensionUtils.findPythonComponentExtension(project, CliExtension.class) != null && pexExtension.isPythonWrapper()) {
            return IOUtils.toString(ThinPexGenerator.class.getResourceAsStream("/templates/pex_cli_entrypoint.py.template"));
        } else {
            return IOUtils.toString(ThinPexGenerator.class.getResourceAsStream("/templates/pex_non_cli_entrypoint.sh.template"));
        }
    }
}
