package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gradle.internal.os.OperatingSystem;


public class DefaultWheelComponentSpec extends DefaultPythonComponentSpec implements WheelComponentSpec {

  private static final OperatingSystem operatingSystem = OperatingSystem.current();
  private final List<PythonTargetPlatform> targetPlatforms = new ArrayList<PythonTargetPlatform>();

  public List<PythonTargetPlatform> getTargetPlatforms() {
    return Collections.unmodifiableList(targetPlatforms);
  }

  public void targetPlatform(String targetPlatform) {
    targetPlatforms.add(new DefaultPythonTargetPlatform(operatingSystem, targetPlatform));
  }
}
