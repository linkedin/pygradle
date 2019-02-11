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
import com.linkedin.gradle.python.util.OperatingSystem;
import com.linkedin.gradle.python.util.StandardTextValues;
import org.gradle.api.Project;

import java.io.File;


public class PexApplication implements ApplicationContainer {
    public static final String TASK_BUILD_PEX = "buildPex";

    private File cache;
    // Default to fat zipapps on Windows, since our wrappers are fairly POSIX specific.
    private boolean isFat = OperatingSystem.current().isWindows();
    private boolean pythonWrapper = true;

    public PexApplication(Project project) {
        this.cache = new File(project.getBuildDir(), "pex-cache");
    }

    public File getPexCache() {
        return cache;
    }

    public void setPexCache(File pexCache) {
        cache = pexCache;
    }

    // These are kept for API backward compatibility.

    /**
     * @return when <code>true</code>, then skinny pex's will be used.
     */
    @Deprecated
    public boolean isFatPex() {
        return isFat();
    }

    /**
     * @param fatPex when <code>true</code>, wrappers will be made all pointing to a single pex file.
     */
    @Deprecated
    public void setFatPex(boolean fatPex) {
        isFat = fatPex;
     }

    // Use these properties instead.

    /**
     * @return when <code>true</code>, then skinny pex's will be used.
     */
    public boolean isFat() {
        return isFat;
    }

    /**
     * @param fat when <code>true</code>, wrappers will be made all pointing to a single pex file.
     */
    public void setIsFat(boolean isFat) {
        this.isFat = isFat;
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

    public void prepareExtension(Project project) {
        final PythonExtension extension = ExtensionUtils.getPythonExtension(project);

        ExtensionUtils.maybeCreate(project, "pex", PexApplication.class, project);
        project.getDependencies().add(StandardTextValues.CONFIGURATION_BUILD_REQS.getValue(),
            extension.forcedVersions.get("pex"));
    }

    public void makeTasks(Project project) {
        project.getTasks().create(TASK_BUILD_PEX, BuildPexTask.class);
    }
}
