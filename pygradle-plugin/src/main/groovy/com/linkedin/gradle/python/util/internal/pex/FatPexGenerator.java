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

import com.linkedin.gradle.python.util.EntryPointHelpers;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.util.List;

public class FatPexGenerator implements PexGenerator {

    private static final Logger logger = Logging.getLogger(FatPexGenerator.class);

    private final Project project;

    public FatPexGenerator(Project project) {
        this.project = project;
    }

    @Override
    public void buildEntryPoints() {
        List<String> dependencies = new PipFreezeAction(project).getDependencies();

        for (String it : EntryPointHelpers.collectEntryPoints(project)) {
            logger.lifecycle("Processing entry point: {}", it);
            String[] split = it.split("=");
            String name = split[0].trim();
            String entry = split[1].trim();

            PexExecSpecAction action = PexExecSpecAction.withEntryPoint(project, name, entry, dependencies);
            ExecResult exec = project.exec(action);
            new PexExecOutputParser(action, exec).validatePexBuildSuccessfully();
        }
    }
}
