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
package com.linkedin.gradle.python.extension.internal;

import com.linkedin.gradle.python.exception.MissingInterpreterException;
import com.linkedin.gradle.python.extension.PythonDefaultVersions;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.extension.PythonVersion;
import com.linkedin.gradle.python.extension.PythonVersionParser;
import com.linkedin.gradle.python.extension.VirtualEnvironment;
import com.linkedin.gradle.python.util.OperatingSystem;
import org.gradle.api.Project;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;


public class DefaultPythonDetails implements PythonDetails, Serializable {

    private transient final Project project;

    private final File venvOverride;
    private final VirtualEnvironment virtualEnvironment;
    private File activateLink;
    private File pythonInterpreter;
    private String virtualEnvPrompt;
    private PythonVersion pythonVersion;
    private PythonDefaultVersions pythonDefaultVersions;

    private List<File> searchPath;

    public DefaultPythonDetails(Project project, File venvDir) {
        this.project = project;
        activateLink = new File(project.getProjectDir(), OperatingSystem.current().getScriptName("activate"));
        virtualEnvPrompt = String.format("(%s)", project.getName());
        searchPath = OperatingSystem.current().getPath();
        venvOverride = venvDir;
        this.virtualEnvironment = new DefaultVirtualEnvironment(this);
        pythonDefaultVersions = new PythonDefaultVersions();
    }

    private void updateFromPythonInterpreter() {
        if (pythonInterpreter == null || !pythonInterpreter.exists()) {
            throw new MissingInterpreterException("Unable to find or execute python");
        }
        pythonVersion = PythonVersionParser.parsePythonVersion(project, pythonInterpreter);
    }

    @Override
    public String getVirtualEnvPrompt() {
        return virtualEnvPrompt;
    }

    @Override
    public void setVirtualEnvPrompt(String virtualEnvPrompt) {
        this.virtualEnvPrompt = virtualEnvPrompt;
    }

    @Override
    public File getVirtualEnv() {
        if (venvOverride == null) {
            return new File(project.getBuildDir(), "venv");
        } else {
            return venvOverride;
        }
    }

    @Override
    public File getVirtualEnvInterpreter() {
        return virtualEnvironment.findExecutable("python");
    }

    @Override
    public File getSystemPythonInterpreter() {
        findPythonWhenAbsent();
        return pythonInterpreter;
    }

    @Override
    public File getActivateLink() {
        return activateLink;
    }

    @Override
    public void setActivateLink(File activateLink) {
        this.activateLink = activateLink;
    }

    /**
     * Adds a new directory to search for an executable. This is like adding the directory to the
     * <strong>beginning</strong> of PATH
     *
     * @param file directory to search for an executable
     */
    @Override
    public void prependExecutableDirectory(File file) {
        searchPath.add(0, file);
    }

    /**
     * Adds a new directory to search for an executable. This is like adding the directory to the <strong>end</strong>
     * of PATH
     *
     * @param file directory to search for an executable
     */
    @Override
    public void appendExecutableDirectory(File file) {
        searchPath.add(file);
    }

    @Override
    public void setPythonDefaultVersions(PythonDefaultVersions defaults) {
        pythonDefaultVersions = defaults;
    }

    @Override
    public void setPythonDefaultVersions(String defaultPython2, String defaultPython3, Collection<String> allowedVersions) {
        pythonDefaultVersions = new PythonDefaultVersions(defaultPython2, defaultPython3, allowedVersions);
    }

    @Override
    public PythonDefaultVersions getPythonDefaultVersions() {
        return pythonDefaultVersions;
    }

    @Override
    public void setPythonVersion(String version) {
        version = pythonDefaultVersions.normalize(version);
        OperatingSystem operatingSystem = OperatingSystem.current();
        pythonInterpreter = operatingSystem.findInPath(searchPath, operatingSystem.getExecutableName(String.format("python%s", version)));
        updateFromPythonInterpreter();
    }

    public void setPythonInterpreter(PythonVersion pythonVersion, File pythonInterpreter) {
        this.pythonVersion = pythonVersion;
        this.pythonInterpreter = pythonInterpreter;
    }

    @Override
    public void setSystemPythonInterpreter(String path) {
        pythonInterpreter = new File(path);
        updateFromPythonInterpreter();
    }

    @Override
    public PythonVersion getPythonVersion() {
        findPythonWhenAbsent();
        return pythonVersion;
    }

    private void findPythonWhenAbsent() {
        if (pythonInterpreter == null) {
            OperatingSystem operatingSystem = OperatingSystem.current();
            File python = operatingSystem.findInPath(searchPath, operatingSystem.getExecutableName("python"));
            if (python == null) {
                python = new File("/usr/bin/python");
            }
            setSystemPythonInterpreter(python.getAbsolutePath());
        }
    }

    @Override
    public VirtualEnvironment getVirtualEnvironment() {
        return virtualEnvironment;
    }
}
