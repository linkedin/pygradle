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
package com.linkedin.gradle.python.util

/**
 * CodeNarc is complaining that some classes exceed 350 lines.  TO fall into compliance with CodeNarc, the standard values
 * are being moved to this enum.  Its an Enum rather than an interface because according to CodeNarc, interfaces of nothing
 * but constants is now taboo.
 */
enum StandardTextValuesConfiguration {
    BOOTSTRAP_REQS('pygradleBootstrap'),
    SETUP_REQS('setupRequires'),
    BUILD_REQS('build'),
    DEFAULT('default'),
    PYDOCS('pydocs'),
    PYTHON('python'),
    TEST('test'),
    VENV('venv'),
    WHEEL('wheel'),

    private final String value

    StandardTextValuesConfiguration(String val) {
        this.value = val
    }

    @Override
    String toString() {
        return value
    }

    String getValue() {
        return value
    }
}

enum StandardTextValuesGroup {
    DOCUMENTATION('documentation'),
    BUILD('build'),
    VERIFICATION('verification'),
    PYGRADLE('pygradle tasks'),
    PYGRADLE_VENV('pygradle virtual environment')

    private final String value

    StandardTextValuesGroup(String val) {
        this.value = val
    }

    @Override
    String toString() {
        return value
    }

    String getValue() {
        return value
    }
}

enum StandardTextValuesTasks {

    //com.linkedin.python
    CLEAN_SAVE_VENV(StandardTextValuesGroup.BUILD,'cleanSaveVenv', "task that cleans the project but leaves the venv in tact.  Helpful for projects on windows that take a very long time to build the venv."),
    SETUP_PY_WRITER(StandardTextValuesGroup.BUILD, 'generateSetupPy', "Creates a blank generateSetup.py"),

    PIN_REQUIREMENTS(StandardTextValuesGroup.PYGRADLE, 'pinRequirements', "Write the direct dependencies into a requirements file as a list of pinned versions."),

    SETUP_LINKS(StandardTextValuesGroup.PYGRADLE_VENV, 'installLinks', "Creates a link so users can activate into the virtual environment."),
    VENV_CREATE(StandardTextValuesGroup.PYGRADLE_VENV, 'createVirtualEnvironment', "Install the virtualenv version that we implicitly depend on so that we can run on systems that don't have virtualenv already installed."),
    INSTALL_BUILD_REQS(StandardTextValuesGroup.PYGRADLE_VENV, 'installBuildRequirements', "Install build requirements into virtualenv."),
    INSTALL_PROJECT(StandardTextValuesGroup.PYGRADLE_VENV, 'installProject', "Install the product itself."),
    INSTALL_PYTHON_REQS(StandardTextValuesGroup.PYGRADLE_VENV, 'installPythonRequirements', "Install the product's Python requirements."),
    INSTALL_SETUP_REQS(StandardTextValuesGroup.PYGRADLE_VENV, 'installSetupRequirements', "Install core setup requirements into virtualenv."),
    INSTALL_TEST_REQS(StandardTextValuesGroup.PYGRADLE_VENV, 'installTestRequirements', "Install the product's Python test requirements."),

    BUILD_DOCS_HTML(StandardTextValuesGroup.DOCUMENTATION, 'buildDocsHtml', "Create documentation using Sphinx."),
    BUILD_DOCS_JSON(StandardTextValuesGroup.DOCUMENTATION, 'buildDocsJson', "Create documentation using Sphinx."),
    BUILD_DOCS(StandardTextValuesGroup.DOCUMENTATION, 'buildDocs', "Create documentation using Sphinx."),
    PACKAGE_DOCS(StandardTextValuesGroup.DOCUMENTATION, 'packageDocs', "Tar up the documentation."),
    PACKAGE_JSON_DOCS(StandardTextValuesGroup.DOCUMENTATION, 'packageJsonDocs', "Tar up the JSON documentation."),

    COVERAGE(StandardTextValuesGroup.VERIFICATION, 'coverage', "Run coverage using py.test. This uses the setup.cfg if present to configure py.test."),
    CHECKSTYLE(StandardTextValuesGroup.VERIFICATION, 'flake8Checkstyle', "Create checkstyle styled report from flake"),
    FLAKE(StandardTextValuesGroup.VERIFICATION, 'flake8', "Run flake8. This uses the setup.cfg if present to configure flake8."),
    CHECK(StandardTextValuesGroup.VERIFICATION, 'check', "core gradle check"),
    PYTEST(StandardTextValuesGroup.VERIFICATION, 'pytest', "Run tests using py.test. This uses the setup.cfg if present to configure py.test."),

    //com.linkedin.python-pex
    BUILD_WHEELS(null, 'buildWheels', ""),
    BUILD_PEX(null, 'buildPex', ""),
    PACKAGE_DEPLOYABLE(null, 'packageDeployable', ""),

    //com.linkedin.python-cli
    GENERATE_COMPLETIONS(null, "generateCompletions", ""),

    //com.linkedin.python-web-app
    BUILD_WEB_APPLICATION(null, 'buildWebApplication', ""),
    PACKAGE_WEB_APPLICATION(null, 'packageWebApplication', ""),

    //com.linkedin.python-flyer
    SETUP_RESOURCE_LINK(null, 'setupResourceLink', ""),
    PACKAGE_RESOURCE_FILES(null, 'packageResourceFiles', ""),

    //com.linkedin.python-sdist
    PACKAGE_SDIST(null, 'packageSdist', ""),

    //gradle
    TASKS(null, 'tasks', "")

    private final String value
    private final StandardTextValuesGroup group
    private final String description

    StandardTextValuesTasks(StandardTextValuesGroup grp, String val, String desc) {
        this.value = val
        this.group = grp
        this.description = desc
    }

    @Override
    String toString() {
        return value
    }

    String getValue() {
        return value
    }

    String getGroup() {
        return group.value
    }

    String getDescription() {
        return description
    }
}


