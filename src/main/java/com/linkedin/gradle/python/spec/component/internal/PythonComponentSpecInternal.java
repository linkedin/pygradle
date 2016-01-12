package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.gradle.process.internal.ExecActionFactory;


public interface PythonComponentSpecInternal extends PythonComponentSpec {
  void setBuildDir(File buildDir);

  File getBuildDir();

  List<PythonEnvironment> getPythonEnvironments();

  void setExecActionFactory(ExecActionFactory execActionFactory);
}
