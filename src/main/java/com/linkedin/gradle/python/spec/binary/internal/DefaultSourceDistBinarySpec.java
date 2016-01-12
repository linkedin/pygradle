package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultPythonTargetPlatform;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;

import java.io.File;
import org.gradle.internal.os.OperatingSystem;


public class DefaultSourceDistBinarySpec extends DefaultPythonBinarySpec implements SourceDistBinarySpec {

    public DefaultSourceDistBinarySpec() {
        super("Source");
    }

    @Override
    public File getVirtualEnvDir() {
        return new File(getBuildDir(), "venv");
    }

    @Override
    public PythonTargetPlatform getSystemPython() {
        return new DefaultPythonTargetPlatform(OperatingSystem.current(), "/usr/bin/python");
    }
}
