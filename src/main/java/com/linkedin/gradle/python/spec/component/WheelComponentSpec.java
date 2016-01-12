package com.linkedin.gradle.python.spec.component;

import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.util.List;
import org.gradle.platform.base.PlatformAwareComponentSpec;


public interface WheelComponentSpec extends PythonComponentSpec, PlatformAwareComponentSpec {
  List<PythonTargetPlatform> getTargetPlatforms();
}
