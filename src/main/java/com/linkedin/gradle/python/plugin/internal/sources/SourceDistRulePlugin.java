package com.linkedin.gradle.python.plugin.internal.sources;

import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.SourceDistBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.configuration.DistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.util.GUtil;


/**
 * This plugin will automatically add wheels to all {@link PythonComponentSpecInternal}'s.
 *
 * This is done by first creating a binary with {@link #createSourceDistBinaries(ModelMap, PythonComponentSpecInternal)},
 * then using {@link #createSourceDistTask(ModelMap, SourceDistBinarySpecInternal)} to create the actual task the make source dist(s).
 */
public class SourceDistRulePlugin extends RuleSource {

  @ComponentBinaries
  public void createSourceDistBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
    final PythonEnvironmentContainer container = spec.getPythonEnvironments();
    binarySpecs.create(spec.getName() + "SourceDist", SourceDistBinarySpec.class,
        new SourceDistAction(container.getDefaultPythonEnvironment()));
  }

  @BinaryTasks
  public void createSourceDistTask(ModelMap<Task> tasks, final SourceDistBinarySpecInternal spec) {
    String postFix = GUtil.toCamelCase(spec.getName());
    tasks.create("create" + postFix, BuildSourceDistTask.class, new DistConfigurationAction(spec.getPythonEnvironment(), spec.getSources()));
  }

}
