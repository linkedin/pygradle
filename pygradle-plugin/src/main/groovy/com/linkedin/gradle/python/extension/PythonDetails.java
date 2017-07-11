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

import com.linkedin.gradle.python.exception.MissingInterpreterException;
import com.linkedin.gradle.python.util.OperatingSystem;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.List;


public class PythonDetails implements Serializable {

    private transient final Project project;

    private final File venvOverride;
    private final VirtualEnvironment virtualEnvironment;
    private File activateLink;
    private File pythonInterpreter;
    private String virtualEnvPrompt;
    private PythonVersion pythonVersion;
    private OperatingSystem operatingSystem = OperatingSystem.current();

    private List<File> searchPath;

    public PythonDetails(Project project) {
        this(project, null);
    }

    public PythonDetails(Project project, File venvDir) {
        this.project = project;
        activateLink = new File(project.getProjectDir(), operatingSystem.getScriptName("activate"));
        virtualEnvPrompt = String.format("(%s)", project.getName());
        searchPath = operatingSystem.getPath();
        venvOverride = venvDir;
        this.virtualEnvironment = new VirtualEnvironment(this);
    }

    private void updateFromPythonInterpreter() {
        if (pythonInterpreter == null || !pythonInterpreter.exists()) {
            throw new MissingInterpreterException("Unable to find or execute python");
        }
        pythonVersion = new PythonVersion(PythonVersionParser.parsePythonVersion(project, pythonInterpreter));
    }

    public String getVirtualEnvPrompt() {
        return virtualEnvPrompt;
    }

    public void setVirtualEnvPrompt(String virtualEnvPrompt) {
        this.virtualEnvPrompt = virtualEnvPrompt;
    }

    public File getVirtualEnv() {
        if (null == venvOverride) {
            return new File(project.getBuildDir(), "venv");
        } else {
            return venvOverride;
        }
    }

    public File getVirtualEnvInterpreter() {
        String binDir = VirtualEnvironment.getPythonApplicationDirectory();
        String binName = operatingSystem.getExecutableName("python");
        return Paths.get(getVirtualEnv().getAbsolutePath(), binDir, binName).toFile();
    }

    public File getSystemPythonInterpreter() {
        findPythonWhenAbsent();
        return pythonInterpreter;
    }

    public File getActivateLink() {
        return activateLink;
    }

    public void setActivateLink(File activateLink) {
        this.activateLink = activateLink;
    }

    /**
     * Adds a new directory to search for an executable. This is like adding the directory to the
     * <strong>beginning</strong> of PATH
     *
     * @param file directory to search for an executable
     */
    public void prependExecutableDirectory(File file) {
        searchPath.add(0, file);
    }

    /**
     * Adds a new directory to search for an executable. This is like adding the directory to the <strong>end</strong>
     * of PATH
     *
     * @param file directory to search for an executable
     */
    public void appendExecutableDirectory(File file) {
        searchPath.add(file);
    }

    public String normalizePythonVersion(String pythonVersion) {
        if ("2".equals(pythonVersion)) {
            return PythonVersion.defaultPython2;
        }

        if ("3".equals(pythonVersion)) {
            return PythonVersion.defaultPython3;
        }

        /* Ensure that it's okay to use this version (major/minor only) of Python. */
        System.out.println("PV:" + pythonVersion
                           + " WL:" + PythonVersion.whitelistedPythonVersions
                           + " M.N:" + new PythonVersion(pythonVersion).getPythonMajorMinor());

        if (!PythonVersion.whitelistedPythonVersions.contains(new PythonVersion(pythonVersion).getPythonMajorMinor())) {
            throw new GradleException("Python version not allowed: " + pythonVersion);
        }
        return pythonVersion;
    }

    public void setPythonVersion(String pythonVersion) {
        pythonVersion = normalizePythonVersion(pythonVersion);
        pythonInterpreter = operatingSystem.findInPath(searchPath, operatingSystem.getExecutableName(String.format("python%s", pythonVersion)));
        updateFromPythonInterpreter();
    }

    public void setSystemPythonInterpreter(String path) {
        pythonInterpreter = new File(path);
        updateFromPythonInterpreter();
    }

    public PythonVersion getPythonVersion() {
        findPythonWhenAbsent();
        return pythonVersion;
    }

    private void findPythonWhenAbsent() {
        if (pythonInterpreter == null) {
            File python = operatingSystem.findInPath(searchPath, operatingSystem.getExecutableName("python"));
            if (python == null) {
                python = new File("/usr/bin/python");
            }
            setSystemPythonInterpreter(python.getAbsolutePath());
        }
    }

    public VirtualEnvironment getVirtualEnvironment() {
        return virtualEnvironment;
    }
}
