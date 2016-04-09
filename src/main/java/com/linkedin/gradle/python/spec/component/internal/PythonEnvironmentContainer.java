package com.linkedin.gradle.python.spec.component.internal;

import com.linkedin.gradle.python.internal.platform.PythonVersion;

import java.util.Collection;
import java.util.Map;


public interface PythonEnvironmentContainer {

    void register(String environment);

    void register(Collection<String> environment);

    Map<PythonVersion, PythonEnvironment> getPythonEnvironments();

    PythonEnvironment getPythonEnvironment(String name);

    PythonEnvironment getDefaultPythonEnvironment();

    boolean isEmpty();

    int size();
}
