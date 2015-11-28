package com.linkedin.gradle.python.spec.internal;

import com.linkedin.gradle.python.spec.PythonEntryPoint;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.List;

public interface PythonComponentSpec extends ComponentSpec, PlatformAwareComponentSpec {
    List<PlatformRequirement> getTargetPlatforms();

    List<String> getKeywords();

    List<PythonEntryPoint> getConsoleScripts();

    boolean autoGenerateSetupPy();
}
