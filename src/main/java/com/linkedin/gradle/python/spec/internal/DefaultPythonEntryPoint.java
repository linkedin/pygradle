package com.linkedin.gradle.python.spec.internal;

import com.linkedin.gradle.python.spec.PythonEntryPoint;

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
