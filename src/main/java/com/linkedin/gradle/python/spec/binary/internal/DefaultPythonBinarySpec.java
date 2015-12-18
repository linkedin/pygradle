package com.linkedin.gradle.python.spec.binary.internal;

import java.io.File;
import org.gradle.platform.base.binary.BaseBinarySpec;

public class DefaultPythonBinarySpec extends BaseBinarySpec implements PythonBinarySpec {

    private final String typeName;

    private File buildDir;

    DefaultPythonBinarySpec(String typeName){
        this.typeName = typeName;
    }

    @Override
    protected String getTypeName() {
        return typeName;
    }

    @Override
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public File getBuildDir() {
        return buildDir;
    }
}
