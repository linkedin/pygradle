package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import org.gradle.platform.base.internal.HasIntermediateOutputsComponentSpec;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;


public interface PythonComponentSpecInternal extends PythonComponentSpec, HasIntermediateOutputsComponentSpec {
    void setBuildDir(File buildDir);

    File getBuildDir();

    PythonEnvironmentContainer getPythonEnvironments();

    void setExecActionFactory(ExecActionFactory execActionFactory);
}
