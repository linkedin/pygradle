package com.linkedin.gradle.python.spec.component;

import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;


public interface PythonComponentSpec extends ComponentSpec, PlatformAwareComponentSpec {

  void buildWheels(boolean wheels);

  boolean getWheels();

  void buildSourceDist(boolean sourceDist);

  boolean getSourceDist();
}
