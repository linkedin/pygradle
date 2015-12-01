package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.PythonByteCode;
import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.*;

public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec {

    private final Set<Class<? extends TransformationFileType>> languageOutputs = new HashSet<Class<? extends TransformationFileType>>();
    private final List<PlatformRequirement> targetPlatforms = new ArrayList<PlatformRequirement>();

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
