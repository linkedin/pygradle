package com.linkedin.gradle.python.spec.component;

import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;


public interface PythonComponentSpec extends ComponentSpec, PlatformAwareComponentSpec {

  WheelComponentSpec getWheels();

  SourceSi

}
