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
import com.linkedin.gradle.python.spec.component.PythonEnvironmentBuilder;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;
import java.util.*;


public class DefaultPythonEnvironmentContainer implements PythonEnvironmentContainer {

    private static final Logger logger = Logging.getLogger(DefaultPythonEnvironmentContainer.class);

    private final Map<PythonVersion, PythonEnvironment> pythonEnvironmentMap = new LinkedHashMap<PythonVersion, PythonEnvironment>();
    private final Set<String> definitionToPythonEnvMap = new HashSet<String>();
    private final ExecActionFactory execActionFactory;
    private final File buildDir;
    private final String name;

    DefaultPythonEnvironmentContainer(File buildDir, String name, ExecActionFactory execActionFactory) {
        this.buildDir = buildDir;
        this.name = name;
        this.execActionFactory = execActionFactory;
    }

    @Override
    public void register(String targetPlatform) {
        if (!definitionToPythonEnvMap.contains(targetPlatform)) {
            logger.debug("Registering python version {}", targetPlatform);
            PythonEnvironment environment = new PythonEnvironmentBuilder(targetPlatform).withBuildDir(buildDir)
                    .withExecActionFactory(execActionFactory)
                    .withName(name)
                    .build();
            definitionToPythonEnvMap.add(targetPlatform);
            pythonEnvironmentMap.put(environment.getVersion(), environment);
        }
    }

    @Override
    public void register(Collection<String> environments) {
        for (String environment : environments) {
            register(environment);
        }
    }

    public PythonEnvironment getPythonEnvironment(String envName) {
        if ("python".equalsIgnoreCase(envName)) {
            return getDefaultPythonEnvironment();
        }

        PythonVersion parse = PythonVersion.parse(envName);

        if (pythonEnvironmentMap.containsKey(parse)) {
            return pythonEnvironmentMap.get(parse);
        }

        String majorMinorVersion = parse.getMajorMinorVersion();
        String majorVersion = parse.getMajorVersion();
        for (PythonEnvironment environment : pythonEnvironmentMap.values()) {
            if (StringUtils.equals(environment.getVersion().getMajorMinorVersion(), majorMinorVersion)) {
                return environment;
            } else if (StringUtils.equals(environment.getVersion().getMajorVersion(), majorVersion)) {
                return environment;
            }
        }

        throw new GradleException("Unable to find python with name " + envName);
    }

    @Override
    public Map<PythonVersion, PythonEnvironment> getPythonEnvironments() {
        return Collections.unmodifiableMap(pythonEnvironmentMap);
    }

    @Override
    public PythonEnvironment getDefaultPythonEnvironment() {
        return pythonEnvironmentMap.values().iterator().next();
    }

    @Override
    public boolean isEmpty() {
        return pythonEnvironmentMap.isEmpty();
    }

    @Override
    public int size() {
        return pythonEnvironmentMap.size();
    }

    @Override
    public String toString() {
        return String.format("DefaultPythonEnvironmentContainer{pythonEnvironmentMap=%s, buildDir=%s, name='%s'}",
                pythonEnvironmentMap, buildDir, name);
    }
}
