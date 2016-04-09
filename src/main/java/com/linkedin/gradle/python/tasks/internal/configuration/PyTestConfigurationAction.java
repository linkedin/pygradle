package com.linkedin.gradle.python.tasks.internal.configuration;

import com.linkedin.gradle.python.PythonTestSourceSet;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.model.ModelMap;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class PyTestConfigurationAction extends BasePythonTaskAction<PythonTestTask> {

    private final Set<File> testSourceDirs = new HashSet<File>();

    public PyTestConfigurationAction(PythonEnvironment pythonEnvironment, ModelMap<LanguageSourceSet> testDir) {
        super(pythonEnvironment);
        for (PythonTestSourceSet pythonTestSourceSet : testDir.withType(PythonTestSourceSet.class)) {
            testSourceDirs.addAll(pythonTestSourceSet.getSource().getSrcDirs());
        }
    }

    @Override
    public void configure(PythonTestTask task) {
        task.dependsOn(getPythonEnvironment().getEnvironmentSetupTaskName());
        task.registerTestSources(testSourceDirs);
        task.setOutputFile(new File(getPythonEnvironment().getBuildDir(),
                "test-results" + File.separatorChar + getPythonEnvironment().getEnvironmentName() + "-" + task.getName() + ".xml"));
    }
}
