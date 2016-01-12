package com.linkedin.gradle.python.spec.binary;

import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.io.File;
import org.gradle.platform.base.Variant;

public interface WheelBinarySpec extends PythonBinarySpec {

    File getPythonBuildDir();

    File getVirtualEnvDir();

    @Variant
    PythonTargetPlatform getTargetPlatform();

    void setTargetPlatform(PythonTargetPlatform platform);

    String getProjectSetupTask();
}
