package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultPythonBinarySpec extends BaseBinarySpec implements PythonBinarySpecInternal {

    PythonEnvironment pythonEnvironment;
    String target;

    @Override
    public void setPythonEnvironment(PythonEnvironment pythonEnvironment) {
        this.pythonEnvironment = pythonEnvironment;
    }

    @Override
    public PythonEnvironment getPythonEnvironment() {
        return pythonEnvironment;
    }

    @Override
    public void targets(String target) {
        this.target = target;
    }

    @Override
    public String getTarget() {
        return target;
    }
}
