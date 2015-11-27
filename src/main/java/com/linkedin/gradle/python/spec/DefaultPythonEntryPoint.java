package com.linkedin.gradle.python.spec;

public class DefaultPythonEntryPoint implements PythonEntryPoint {

    private final String scriptName;
    private final String pythonReference;

    public DefaultPythonEntryPoint(String scriptName, String pythonReference) {
        this.scriptName = scriptName;
        this.pythonReference = pythonReference;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    @Override
    public String getPythonReference() {
        return pythonReference;
    }
}
