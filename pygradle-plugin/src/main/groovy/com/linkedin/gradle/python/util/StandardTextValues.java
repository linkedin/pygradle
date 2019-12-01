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
package com.linkedin.gradle.python.util;

/**
 * CodeNarc is complaining that some classes exceed 350 lines.  To fall into compliance with CodeNarc, the standard values
 * are being moved to this enum.  It's an Enum rather than an interface because according to CodeNarc, interfaces of nothing
 * but constants is now taboo.
 */
public enum StandardTextValues {
    CONFIGURATION_BOOTSTRAP_REQS("pygradleBootstrap"),
    CONFIGURATION_SETUP_REQS("setupRequires"),
    CONFIGURATION_BUILD_REQS("build"),
    CONFIGURATION_DEFAULT("default"),
    CONFIGURATION_PYDOCS("pydocs"),
    CONFIGURATION_PYTHON("python"),
    CONFIGURATION_TEST("test"),
    CONFIGURATION_VENV("venv"),
    CONFIGURATION_WHEEL("wheel"),
    TASK_BLACK("runBlack"),
    TASK_BUILD_DOCS("buildDocs"),
    TASK_CLEAN_SAVE_VENV("cleanSaveVenv"),
    TASK_CHECK("check"),
    TASK_COVERAGE("coverage"),
    TASK_FLAKE("flake8"),
    TASK_CHECKSTYLE("flake8Checkstyle"),
    TASK_INSTALL_SETUP_REQS("installSetupRequirements"),
    TASK_INSTALL_BUILD_REQS("installBuildRequirements"),
    TASK_INSTALL_PROJECT("installProject"),
    TASK_INSTALL_PYTHON_REQS("installPythonRequirements"),
    TASK_INSTALL_TEST_REQS("installTestRequirements"),
    TASK_ISORT("runIsort"),
    TASK_MYPY("runMypy"),
    TASK_PACKAGE_DOCS("packageDocs"),
    TASK_PACKAGE_JSON_DOCS("packageJsonDocs"),
    TASK_PYTEST("pytest"),
    TASK_SETUP_LINKS("installLinks"),
    TASK_VENV_CREATE("createVirtualEnvironment"),
    TASK_GET_PROBED_TAGS("getProbedTags"),
    TASK_PIN_REQUIREMENTS("pinRequirements"),
    TASK_SETUP_PY_WRITER("generateSetupPy"),
    DOCUMENTATION_GROUP("documentation"),
    BUILD_GROUP("build");

    private final String value;

    StandardTextValues(String val) {
        this.value = val;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

}
