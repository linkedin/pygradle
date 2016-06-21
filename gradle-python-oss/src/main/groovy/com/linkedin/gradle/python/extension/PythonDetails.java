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

import com.linkedin.gradle.python.util.internal.ExecutablePathUtils;
import org.gradle.api.Project;

import java.io.File;
import java.util.List;


public class PythonDetails {

    private final Project project;

    private final File virtualEnv;
    private File activateLink;
    private File pythonInterpreter;
    private String virtualEnvPrompt;
    private PythonVersion pythonVersion;

    private List<File> searchPath;

    public PythonDetails(Project project, File virtualenvLocation) {
        this.project = project;
        pythonInterpreter = new File("/usr/bin/python");
        updateFromPythonInterpreter();

        virtualEnv = virtualenvLocation;
        activateLink = new File(project.getProjectDir(), "activate");
        virtualEnvPrompt = String.format("(%s)", project.getName());
        searchPath = ExecutablePathUtils.getPath();
    }

    private void updateFromPythonInterpreter() {
        if (pythonInterpreter == null || !pythonInterpreter.exists()) {
            throw new RuntimeException("Unable to find or execute python");
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
        return virtualEnv;
    }

    public File getVirtualEnvInterpreter() {
        return new File(virtualEnv, "bin/python");
    }

    public File getSystemPythonInterpreter() {
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
    public void prependExecuableDirectory(File file) {
        searchPath.add(0, file);
    }

    /**
     * Adds a new directory to search for an executable. This is like adding the directory to the <strong>end</strong>
     * of PATH
     *
     * @param file directory to search for an executable
     */
    public void appendExecuableDirectory(File file) {
        searchPath.add(file);
    }

    public void setPythonVersion(String pythonVersion) {
        if ("2".equals(pythonVersion)) {
            pythonVersion = "2.6";
        }

        if ("3".equals(pythonVersion)) {
            pythonVersion = "3.5";
        }

        pythonInterpreter = ExecutablePathUtils.getExecutable(searchPath, String.format("python%s", pythonVersion));
        updateFromPythonInterpreter();
    }

    public void setSystemPythonInterpreter(String path) {
        pythonInterpreter = new File(path);
        if (!pythonInterpreter.exists()) {
            throw new RuntimeException("Unable to find " + path);
        }

        updateFromPythonInterpreter();
    }

    public PythonVersion getPythonVersion() {
        return pythonVersion;
    }
}
