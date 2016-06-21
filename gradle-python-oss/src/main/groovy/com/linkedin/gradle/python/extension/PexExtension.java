/**
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

import java.io.File;

import org.gradle.api.Project;


public class PexExtension {

    private File pexCache;
    private boolean fatPex = false;
    private boolean pythonWrapper = false;

    public PexExtension(Project project) {
        pexCache = new File(project.getBuildDir(), "pex-cache");
    }

    public File getPexCache() {
        return pexCache;
    }

    public void setPexCache(File pexCache) {
        this.pexCache = pexCache;
    }

    /**
     * @return when <code>true</code>, then skinny pex's will be used.
     */
    public boolean isFatPex() {
        return fatPex;
    }

    /**
     * When <code>true</code>, wrappers will be made all pointing to a single pex file.
     */
    public void setFatPex(boolean fatPex) {
        this.fatPex = fatPex;
    }

    /**
     * TODO: Revisit if this is needed.
     * @return should use python wrapper
     */
    public boolean isPythonWrapper() {
        return pythonWrapper;
    }

    public void setPythonWrapper(boolean pythonWrapper) {
        this.pythonWrapper = pythonWrapper;
    }
}
