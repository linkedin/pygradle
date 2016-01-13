package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;


public class DistConfigurationAction extends BasePythonTaskAction<AbstractDistTask> {
    private final ModelMap<LanguageSourceSet> sourceSets;

    public DistConfigurationAction(PythonEnvironment pythonEnvironment, ModelMap<LanguageSourceSet> sourceSets) {
        super(pythonEnvironment);
        this.sourceSets = sourceSets;
    }

    @Override
    public void configure(AbstractDistTask task) {
        for (PythonSourceSet pythonSourceSet : sourceSets.withType(PythonSourceSet.class)) {
            task.sourceSet(pythonSourceSet.getSource());
        }

        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
        task.shouldRunAfter("check");
    }
}
