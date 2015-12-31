package com.linkedin.gradle.python.spec.binary.internal;

import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultSourceDistBinarySpec extends DefaultPythonBinarySpec implements SourceDistBinarySpec {

    private final List<PythonTargetPlatform> platformList = new ArrayList<PythonTargetPlatform>();

    public DefaultSourceDistBinarySpec() {
        super("Source");
    }

    @Override
    public List<PythonTargetPlatform> getTestPlatforms() {
        return Collections.unmodifiableList(platformList);
    }

    @Override
    public void setTestPlatforms(List<PythonTargetPlatform> platform) {
        platformList.clear();
        platformList.addAll(platform);
    }

    @Override
    public File buildDirFor(PythonTargetPlatform targetPlatform) {
        return new File(getBuildDir(), String.format("python-%s-%s", getName(), targetPlatform.getVersion().getVersionString()));
    }

    @Override
    public List<ResolvedPythonEnvironment> getPythonEnvironments() {
        ArrayList<ResolvedPythonEnvironment> pythonEnvironments = new ArrayList<ResolvedPythonEnvironment>();
        for (PythonTargetPlatform targetPlatform : getTestPlatforms()) {
            File buildDir = buildDirFor(targetPlatform);
            pythonEnvironments.add(new ResolvedPythonEnvironment(buildDir, new File(buildDir, "venv"), targetPlatform));
        }

        return pythonEnvironments;
    }
}
