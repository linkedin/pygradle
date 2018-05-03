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
package com.linkedin.gradle.python.extension;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

/**
 * Details about the python version
 */
public interface PythonDetails extends Serializable {
    String getVirtualEnvPrompt();

    void setVirtualEnvPrompt(String virtualEnvPrompt);

    File getVirtualEnv();

    File getVirtualEnvInterpreter();

    File getSystemPythonInterpreter();

    File getActivateLink();

    void setActivateLink(File activateLink);

    void prependExecutableDirectory(File file);

    void appendExecutableDirectory(File file);

    void setPythonDefaultVersions(PythonDefaultVersions defaults);

    void setPythonDefaultVersions(String defaultPython2, String defaultPython3, Collection<String> allowedVersions);

    PythonDefaultVersions getPythonDefaultVersions();

    void setPythonVersion(String version);

    void setSystemPythonInterpreter(String path);

    PythonVersion getPythonVersion();

    VirtualEnvironment getVirtualEnvironment();
}
