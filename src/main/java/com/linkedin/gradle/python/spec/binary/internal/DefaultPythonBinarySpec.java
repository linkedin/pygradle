package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import java.io.File;
import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultPythonBinarySpec extends BaseBinarySpec implements PythonBinarySpec {

    PythonEnvironment pythonEnvironment;

    @Override
    public void setPythonEnvironment(PythonEnvironment pythonEnvironment) {
        this.pythonEnvironment = pythonEnvironment;
    }

    @Override
    public PythonEnvironment getPythonEnvironment() {
        return pythonEnvironment;
    }
}
