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
package com.linkedin.gradle.python.extension;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.tasks.BuildPexTask;
import com.linkedin.gradle.python.util.ApplicationContainer;
import com.linkedin.gradle.python.util.ExtensionUtils;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.io.File;


public class PexExtension implements ApplicationContainer {
    // 2019-04-01(warsaw): For backward compatibility, we must expose a no-op
    // buildPex task unconditionally.  This will be created in
    // PythonContainerPlugin and tied into the task hierarchy in the right
    // place.  realBuildPex task is the actual pex building task, but these
    // are only needed if pexes are selected (and won't get created until
    // after build.gradle evaluation).
    //
    // Yes, this is gross and we should deprecate this mess at our earliest convenience.
    public static final String TASK_BUILD_PEX = "realBuildPex";
    public static final String TASK_BUILD_NOOP_PEX = "buildPex";

    private File cache;
    private boolean pythonWrapper = true;
    private Project project;

    public PexExtension(Project project) {
        this.cache = new File(project.getBuildDir(), "pex-cache");
        this.project = project;
    }

    public File getPexCache() {
        return cache;
    }

    public void setPexCache(File pexCache) {
        cache = pexCache;
    }

    /**
     * TODO: Revisit if this is needed.
     *
     * @return should use python wrapper
     */
    public boolean isPythonWrapper() {
        return pythonWrapper;
    }

    public void setPythonWrapper(boolean pythonWrapper) {
        this.pythonWrapper = pythonWrapper;
    }

    public File getCache() {
        return cache;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    public void addExtensions(Project project) {
        ExtensionUtils.maybeCreatePexExtension(project);
    }

    public void addDependencies(Project project) {
        final PythonExtension extension = ExtensionUtils.getPythonExtension(project);

        project.getDependencies().add(StandardTextValues.CONFIGURATION_BUILD_REQS.getValue(),
            extension.forcedVersions.get("pex"));
    }

    public void makeTasks(Project project) {
        project.getTasks().maybeCreate(TASK_BUILD_PEX, BuildPexTask.class);
    }

    // For backward compatibility in build.gradle flies.
    @Deprecated
    public boolean isFatPex() {
        return ExtensionUtils.getPythonComponentExtension(project, ZipappContainerExtension.class).isFat();
    }

    @Deprecated
    public void setFatPex(boolean fatPex) {
        ExtensionUtils.getPythonComponentExtension(project, ZipappContainerExtension.class).setIsFat(fatPex);
     }

    @Deprecated
    public boolean isFat() {
        return ExtensionUtils.getPythonComponentExtension(project, ZipappContainerExtension.class).isFat();
    }

    @Deprecated
    public void setIsFat(boolean isFat) {
        ExtensionUtils.getPythonComponentExtension(project, ZipappContainerExtension.class).setIsFat(isFat);
     }
}
