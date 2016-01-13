package com.linkedin.gradle.python.plugin.internal.language;

import com.linkedin.gradle.python.plugin.internal.base.SourceDistAction;
import com.linkedin.gradle.python.plugin.internal.base.WheelAction;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentBinaries;


public class PythonLangRulePlugin extends RuleSource {

  @ComponentBinaries
  public void createSourceDistBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
    final PythonEnvironmentContainer container = spec.getPythonEnvironments();
    if (spec.getSourceDist()) {
      binarySpecs.create(spec.getName() + "SourceDist", SourceDistBinarySpec.class,
          new SourceDistAction(container.getDefaultPythonEnvironment()));
    }

    if (spec.getWheels()) {
      for (PythonEnvironment pythonEnvironment : container.getPythonEnvironments().values()) {
        String name = spec.getName() + "Wheel" + pythonEnvironment.getVersion().getVersionString();
        binarySpecs.create(name, WheelBinarySpec.class, new WheelAction(pythonEnvironment));
      }
    }
  }
}
