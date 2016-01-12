package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.PythonByteCode;
import javax.inject.Inject;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.*;
import org.gradle.process.internal.ExecActionFactory;


public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec {
    private final Set<Class<? extends TransformationFileType>> languageOutputs = new HashSet<Class<? extends TransformationFileType>>();

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
}
