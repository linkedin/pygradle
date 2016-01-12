package com.linkedin.gradle.python.plugin.internal.pytest;

import com.linkedin.gradle.python.spec.component.PyTestComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultPyTestComponentSpec;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;


public class PyTestRulePlugin extends RuleSource {

  @ComponentType
  public void register(ComponentTypeBuilder<PyTestComponentSpec> builder) {
    builder.defaultImplementation(DefaultPyTestComponentSpec.class);
  }

  @Mutate
  public void registerPyTestTasks(TaskContainer taskContainer, ModelMap<PyTestComponentSpec> spec) {
    for (PyTestComponentSpec pyTestComponentSpec : spec) {

    }
  }
}
