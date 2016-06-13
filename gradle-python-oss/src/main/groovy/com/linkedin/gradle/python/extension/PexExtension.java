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
