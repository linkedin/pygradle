package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.plugin.internal.base.WheelAction;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.WheelBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.internal.configuration.DistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.ComponentBinaries;


public class WheelRulePlugin extends RuleSource {

  private static final Logger logger = Logging.getLogger(WheelRulePlugin.class);

  @ComponentBinaries
  public void createSourceDistBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
    final PythonEnvironmentContainer container = spec.getPythonEnvironments();

    for (PythonEnvironment pythonEnvironment : container.getPythonEnvironments().values()) {
      String name = spec.getName() + "Wheel" + pythonEnvironment.getVersion().getVersionString();
      binarySpecs.create(name, WheelBinarySpec.class, new WheelAction(pythonEnvironment));
    }
  }

  @BinaryTasks
  public void createWheelTask(ModelMap<Task> tasks, final WheelBinarySpecInternal spec) {
    //Capitalize String
    String postFix = spec.getName().substring(0, 1).toUpperCase() + spec.getName().substring(1);
    tasks.create("create" + postFix, BuildWheelTask.class, new DistConfigurationAction(spec.getPythonEnvironment(), spec.getSources()));
  }

}
