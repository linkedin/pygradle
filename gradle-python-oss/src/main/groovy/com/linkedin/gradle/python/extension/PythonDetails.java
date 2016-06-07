package com.linkedin.gradle.python.extension;

import org.gradle.api.Project;

import java.io.File;


public class PythonDetails {

    private final Project project;

    private final File virtualEnv;
    private File activateLink;
    private File pythonInterpreter;
    private String virtualEnvPrompt;
    private PythonVersion pythonVersion;

    public PythonDetails(Project project, File virtualenvLocation) {
        this.project = project;
        pythonInterpreter = new File("/usr/bin/python");
        updateFromPythonInterpreter();

        virtualEnv = virtualenvLocation;
        activateLink = new File(project.getProjectDir(), "activate");
        virtualEnvPrompt = String.format("(%s)", project.getName());
    }

    private void updateFromPythonInterpreter() {
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

    public PythonVersion getPythonVersion() {
        return pythonVersion;
    }
}
