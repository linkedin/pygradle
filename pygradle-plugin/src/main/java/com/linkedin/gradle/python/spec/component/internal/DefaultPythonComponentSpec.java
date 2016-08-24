/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import org.gradle.platform.base.TransformationFileType;
import org.gradle.platform.base.component.BaseComponentSpec;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * An implementation of {@see PythonComponentSpecInternal}.
 */
public class DefaultPythonComponentSpec extends BaseComponentSpec implements PythonComponentSpec, PythonComponentSpecInternal {

    private final Set<String> targetPlatforms = new HashSet<String>();

    private DefaultPythonEnvironmentContainer pythonEnvironmentContainer;
    private ExecActionFactory execActionFactory;
    private File buildDir;

    @Override
    protected String getTypeName() {
        return "Python application";
    }

    @Override
    public void targetPlatform(String targetPlatform) {
        targetPlatforms.add(targetPlatform);
    }

    @Override
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    @Override
    public File getBuildDir() {
        return buildDir;
    }

    @Override
    public void setExecActionFactory(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
    }

    @Override
    public PythonEnvironmentContainer getPythonEnvironments() {
        if (pythonEnvironmentContainer == null) {
            pythonEnvironmentContainer = new DefaultPythonEnvironmentContainer(buildDir, getName(), execActionFactory);
        }

        pythonEnvironmentContainer.register(targetPlatforms);

        return pythonEnvironmentContainer;
    }

    @Override
    public Set<? extends Class<? extends TransformationFileType>> getIntermediateTypes() {
        return Collections.emptySet();
    }
}
