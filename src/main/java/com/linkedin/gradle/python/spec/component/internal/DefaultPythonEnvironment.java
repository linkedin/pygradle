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

import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.DefaultPythonExecutable;
import com.linkedin.gradle.python.internal.toolchain.PythonExecutable;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;
import java.util.Objects;


public class DefaultPythonEnvironment implements PythonEnvironment {

    final DefaultPythonExecutable systemPythonExecutable;
    final DefaultPythonExecutable virtualEnvExecutable;
    final File venvDir;
    final File vendorDir;
    final File buildDir;
    final File pythonBuildDir;
    final PythonVersion version;
    final String name;

    public DefaultPythonEnvironment(File pythonExecutable, PythonVersion version, File buildDir, ExecActionFactory execActionFactory, String name) {
        Objects.requireNonNull(pythonExecutable);
        Objects.requireNonNull(version);
        this.systemPythonExecutable = new DefaultPythonExecutable(execActionFactory, pythonExecutable);
        this.version = version;
        this.buildDir = buildDir;
        this.name = name;
        this.pythonBuildDir = new File(buildDir, String.format("python-%s-%s", version.getVersionString(), name));
        this.venvDir = new File(pythonBuildDir, "venv");
        this.vendorDir = new File(buildDir, "vendor");
        this.virtualEnvExecutable = new DefaultPythonExecutable(execActionFactory, new File(venvDir, "bin/python"));
    }

    @Override
    public String getEnvironmentSetupTaskName() {
        return String.format("setup%s", version.getVersionString());
    }

    @Override
    public PythonExecutable getVirtualEnvPythonExecutable() {
        return virtualEnvExecutable;
    }

    @Override
    public PythonExecutable getSystemPythonExecutable() {
        return systemPythonExecutable;
    }

    @Override
    public File getVenvDir() {
        return venvDir;
    }

    @Override
    public PythonVersion getVersion() {
        return version;
    }

    @Override
    public File getVendorDir() {
        return vendorDir;
    }

    @Override
    public File getBuildDir() {
        return buildDir;
    }

    @Override
    public File getPythonBuildDir() {
        return pythonBuildDir;
    }

    @Override
    public String getEnvironmentName() {
        return name;
    }
}
