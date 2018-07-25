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
package com.linkedin.gradle.build.version;


import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;

public class VersionPlugin implements Plugin<Project> {

    Logger logger = Logging.getLogger(VersionPlugin.class);

    @Override
    public void apply(Project target) {
        if (target.getRootProject() != target) {
            throw new GradleException("Cannot apply dependency plugin to a non-root project");
        }

        File versionProperties = target.file("version.properties");

        Version version = VersionFile.getVersion(versionProperties);

        if (!target.hasProperty("release") || !Boolean.parseBoolean((String) target.property("release"))) {
            version = version.asSnapshot();
        }

        logger.lifecycle("Building using version {}", version);
        target.allprojects(new VersionAction(version));
    }

    private static class VersionAction implements Action<Project> {
        private final Version version;

        public VersionAction(Version version) {
            this.version = version;
        }

        @Override
        public void execute(Project project) {
            project.setVersion(version);
        }
    }
}
