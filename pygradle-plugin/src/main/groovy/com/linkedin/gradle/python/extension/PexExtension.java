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

import com.linkedin.gradle.python.util.OperatingSystem;
import org.gradle.api.Project;

import java.io.File;


public class PexExtension extends ZipappExtension {
    private boolean pythonWrapper = true;

    public PexExtension(Project project) {
        super(new File(project.getBuildDir(), "pex-cache"));
    }

    // These are kept for API backward compatibility.

    public File getPexCache() {
        return getCache();
    }


    public void setPexCache(File pexCache) {
        super.setCache(pexCache);
    }

    /**
     * @return when <code>true</code>, then skinny pex's will be used.
     */
    public boolean isFatPex() {
        return isFat();
    }

    /**
     * @param fatPex when <code>true</code>, wrappers will be made all pointing to a single pex file.
     */
    public void setFatPex(boolean fatPex) {
        super.setIsFat(fatPex);
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
}
