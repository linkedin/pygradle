package com.linkedin.gradle.python.spec.internal;

import com.linkedin.gradle.python.internal.PythonByteCode;
import com.linkedin.gradle.python.spec.WheelComponentSpec;
import com.linkedin.gradle.python.spec.PythonEntryPoint;
import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;

import java.util.*;

public class DefaultWheelComponentSpec extends BaseComponentSpec implements WheelComponentSpec {

    private final Set<Class<? extends TransformationFileType>> languageOutputs = new HashSet<Class<? extends TransformationFileType>>();
    private final List<PlatformRequirement> targetPlatforms = new ArrayList<PlatformRequirement>();
    private final List<String> keywords = new ArrayList<String>();
    private final List<PythonEntryPoint> consoleScripts = new ArrayList<PythonEntryPoint>();
    private boolean autoGenerateSetupPy = true;

    public DefaultWheelComponentSpec() {
        this.languageOutputs.add(PythonByteCode.class);
        keywords.addAll(Arrays.asList("setuptools", "development"));
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

    @Override
    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    @Override
    public List<PythonEntryPoint> getConsoleScripts() {
        return consoleScripts;
    }

    @Override
    public boolean autoGenerateSetupPy() {
        return autoGenerateSetupPy;
    }

    public void autoGenerateSetupPy(boolean bool) {
        this.autoGenerateSetupPy = bool;
    }

    public void consoleScript(String name, String pythonRef) {
        consoleScripts.add(new DefaultPythonEntryPoint(name, pythonRef));
    }

    public void keyword(String... keywords) {
        this.keywords.clear();
        this.keywords.addAll(Arrays.asList(keywords));
    }

    public void targetPlatform(String targetPlatform) {
        this.targetPlatforms.add(DefaultPlatformRequirement.create(targetPlatform));
    }
}
