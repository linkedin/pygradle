package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.spec.PythonBinarySpec;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.SourceTransformTaskConfig;
import org.gradle.platform.base.BinarySpec;

public class PythonTransformTaskConfig implements SourceTransformTaskConfig {
    @Override
    public String getTaskPrefix() {
        return "python";
    }

    @Override
    public Class<? extends DefaultTask> getTaskType() {
        return PythonCompile.class;
    }

    @Override
    public void configureTask(Task task, BinarySpec binarySpec, LanguageSourceSet languageSourceSet, ServiceRegistry serviceRegistry) {
        PythonCompile compile = (PythonCompile) task;
        PythonSourceSet pythonSourceSet = (PythonSourceSet) languageSourceSet;
        PythonBinarySpec binary = (PythonBinarySpec) binarySpec;

        compile.setDescription(String.format("Compiles %s.", pythonSourceSet));
//        compile.setDestinationDir(binary.getClassesDir());
//        compile.setToolChain(binary.getToolChain());
//        compile.setPlatform(binary.getTargetPlatform());
    }
}
