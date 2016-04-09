package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;


abstract public class DistConfigurationAction<T extends AbstractDistTask> extends BasePythonTaskAction<T> {
    private final ModelMap<LanguageSourceSet> sourceSets;

    public DistConfigurationAction(PythonEnvironment pythonEnvironment, ModelMap<LanguageSourceSet> sourceSets) {
        super(pythonEnvironment);
        this.sourceSets = sourceSets;
    }

    @Override
    public void configure(T task) {
        for (PythonSourceSet pythonSourceSet : sourceSets.withType(PythonSourceSet.class)) {
            task.sourceSet(pythonSourceSet.getSource());
        }

        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
        task.shouldRunAfter("check");
        doConfigure(task);
    }

    abstract public void doConfigure(T task);
}
