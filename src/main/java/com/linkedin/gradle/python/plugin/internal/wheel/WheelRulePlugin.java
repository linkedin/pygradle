package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.spec.binary.internal.WheelBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.internal.configuration.WheelDistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasks;


/**
 * This plugin will automatically add wheels to all {@link PythonComponentSpecInternal}'s.
 * <p>
 * This is done by first creating the binaries with {@link #createWheelBinaries(ModelMap, PythonComponentSpecInternal)},
 * then using {@link #createWheelTask(ModelMap, WheelBinarySpecInternal)} to create the actual tasks the make wheels.
 */
public class WheelRulePlugin extends RuleSource {

    @BinaryTasks
    public void createWheelTask(ModelMap<Task> tasks, final WheelBinarySpecInternal spec) {
        //Capitalize String
        String postFix = spec.getName().substring(0, 1).toUpperCase() + spec.getName().substring(1);
        String taskName = "create" + postFix;
        tasks.create(taskName, BuildWheelTask.class, new WheelDistConfigurationAction(spec));
    }

}
