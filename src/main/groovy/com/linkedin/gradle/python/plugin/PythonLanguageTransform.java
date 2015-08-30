package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.PythonByteCode;
import com.linkedin.gradle.python.spec.PythonBinarySpec;
import com.linkedin.gradle.python.tasks.PythonTransformTaskConfig;
import org.gradle.language.base.internal.SourceTransformTaskConfig;
import org.gradle.language.base.internal.registry.LanguageTransform;
import org.gradle.platform.base.BinarySpec;

import java.util.Collections;
import java.util.Map;

public class PythonLanguageTransform implements LanguageTransform<PythonSourceSet, PythonByteCode> {

    @Override
    public Class<PythonSourceSet> getSourceSetType() {
        return PythonSourceSet.class;
    }

    @Override
    public Class<PythonByteCode> getOutputType() {
        return PythonByteCode.class;
    }

    @Override
    public Map<String, Class<?>> getBinaryTools() {
        return Collections.emptyMap();
    }

    @Override
    public SourceTransformTaskConfig getTransformTask() {
        return new PythonTransformTaskConfig();
    }

    @Override
    public boolean applyToBinary(BinarySpec binarySpec) {
        return binarySpec instanceof PythonBinarySpec;
    }
}
