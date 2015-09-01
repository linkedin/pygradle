package com.linkedin.gradle.python.spec;

import com.google.common.collect.Lists;
import com.linkedin.gradle.python.internal.PythonByteCode;
import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec {
    private final Set<Class<? extends TransformationFileType>> languageOutputs = new HashSet<Class<? extends TransformationFileType>>();
    private final List<PlatformRequirement> targetPlatforms = Lists.newArrayList();

    public DefaultPythonComponentSpec() {
        this.languageOutputs.add(PythonByteCode.class);
    }

    @Override
    protected String getTypeName() {
        return "Python application";
    }

    public Set<Class<? extends TransformationFileType>> getInputTypes() {
        return languageOutputs;
    }

    public List<PlatformRequirement> getTargetPlatforms() {
        return Collections.unmodifiableList(targetPlatforms);
    }

    public void targetPlatform(String targetPlatform) {
        this.targetPlatforms.add(DefaultPlatformRequirement.create(targetPlatform));
    }
}
