package com.linkedin.gradle.python.spec;

import java.util.List;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.internal.PlatformRequirement;

public interface PythonComponentSpec extends ComponentSpec, PlatformAwareComponentSpec {
    List<PlatformRequirement> getTargetPlatforms();
}
