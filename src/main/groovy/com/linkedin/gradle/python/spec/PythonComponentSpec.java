package com.linkedin.gradle.python.spec;

import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.List;

public interface PythonComponentSpec extends ComponentSpec, PlatformAwareComponentSpec {
    List<PlatformRequirement> getTargetPlatforms();


}
