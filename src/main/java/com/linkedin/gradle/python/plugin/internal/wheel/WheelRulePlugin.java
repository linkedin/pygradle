package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.spec.binary.internal.WheelBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.internal.configuration.DistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasks;


/**
 * This plugin will automatically add wheels to all {@link PythonComponentSpecInternal}'s.
 *
 * This is done by first creating the binaries with {@link #createWheelBinaries(ModelMap, PythonComponentSpecInternal)},
 * then using {@link #createWheelTask(ModelMap, WheelBinarySpecInternal)} to create the actual tasks the make wheels.
 */
public class WheelRulePlugin extends RuleSource {

  @BinaryTasks
  public void createWheelTask(ModelMap<Task> tasks, final WheelBinarySpecInternal spec) {
    //Capitalize String
    String postFix = spec.getName().substring(0, 1).toUpperCase() + spec.getName().substring(1);
    tasks.create("create" + postFix, BuildWheelTask.class, new DistConfigurationAction(spec.getPythonEnvironment(), spec.getSources()));
  }

}
