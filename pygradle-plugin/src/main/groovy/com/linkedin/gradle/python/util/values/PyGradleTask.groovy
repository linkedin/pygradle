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
package com.linkedin.gradle.python.util.values

import static com.linkedin.gradle.python.util.values.PyGradleGroup.*
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
enum PyGradleTask {

    //com.linkedin.python
    CLEAN_SAVE_VENV(BUILD,'cleanSaveVenv', "task that cleans the project but leaves the venv in tact.  Helpful for projects on windows that take a very long time to build the venv."),
    SETUP_PY_WRITER(BUILD, 'generateSetupPy', "Creates a blank generateSetup.py"),

    PIN_REQUIREMENTS(PYGRADLE, 'pinRequirements', "Write the direct dependencies into a requirements file as a list of pinned versions."),

    SETUP_LINKS(PYGRADLE_VENV, 'installLinks', "Creates a link so users can activate into the virtual environment."),
    VENV_CREATE(PYGRADLE_VENV, 'createVirtualEnvironment', "Install the virtualenv version that we implicitly depend on so that we can run on systems that don't have virtualenv already installed."),
    INSTALL_BUILD_REQS(PYGRADLE_VENV, 'installBuildRequirements', "Install build requirements into virtualenv."),
    INSTALL_PROJECT(PYGRADLE_VENV, 'installProject', "Install the product itself."),
    INSTALL_PYTHON_REQS(PYGRADLE_VENV, 'installPythonRequirements', "Install the product's Python requirements."),
    INSTALL_SETUP_REQS(PYGRADLE_VENV, 'installSetupRequirements', "Install core setup requirements into virtualenv."),
    INSTALL_TEST_REQS(PYGRADLE_VENV, 'installTestRequirements', "Install the product's Python test requirements."),

    BUILD_DOCS_HTML(DOCUMENTATION, 'buildDocsHtml', "Create documentation in HTML using Sphinx."),
    BUILD_DOCS_JSON(DOCUMENTATION, 'buildDocsJson', "Create documentation in JSON using Sphinx."),
    BUILD_DOCS(DOCUMENTATION, 'buildDocs', "Create documentation using Sphinx."),
    PACKAGE_DOCS(DOCUMENTATION, 'packageDocs', "Tar up the documentation."),
    PACKAGE_JSON_DOCS(DOCUMENTATION, 'packageJsonDocs', "Tar up the JSON documentation."),

    COVERAGE(VERIFICATION, 'coverage', "Run coverage using py.test. This uses the setup.cfg if present to configure py.test."),
    CHECKSTYLE(VERIFICATION, 'flake8Checkstyle', "Create checkstyle styled report from flake"),
    FLAKE(VERIFICATION, 'flake8', "Run flake8. This uses the setup.cfg if present to configure flake8."),
    CHECK(VERIFICATION, 'check', "core gradle check"),
    PYTEST(VERIFICATION, 'pytest', "Run tests using py.test. This uses the setup.cfg if present to configure py.test."),

    //com.linkedin.python-pex
    BUILD_WHEELS(BUILD, 'buildWheels', "Builds a python deployable Wheels file"),
    BUILD_PEX(BUILD, 'buildPex', "Builds a python linux and mac compatible Pex file"),
    PACKAGE_DEPLOYABLE(BUILD, 'packageDeployable', "Packages the Pex file for deployment"),

    //com.linkedin.python-cli
    GENERATE_COMPLETIONS(PYGRADLE, "generateCompletions", "Creates the shell completions files"),

    //com.linkedin.python-web-app
    BUILD_WEB_APPLICATION(BUILD, 'buildWebApplication', "Build a web app, by default using gunicorn, but it's configurable."),
    PACKAGE_WEB_APPLICATION(PYGRADLE, 'packageWebApplication', ""),

    //com.linkedin.python-flyer
    SETUP_RESOURCE_LINK(PYGRADLE, 'setupResourceLink', ""),
    PACKAGE_RESOURCE_FILES(BUILD, 'packageResourceFiles', ""),

    //com.linkedin.python-sdist
    PACKAGE_SDIST(BUILD, 'packageSdist', ""),

    //com.linkedin.python-wheel
    PACKAGE_WHEEL(BUILD, 'packageWheel', ""),

    //gradle
    TASKS(HELP, 'tasks', "Gradle Core Tasks task")

    private final String value
    private final PyGradleGroup group
    private final String description

    PyGradleTask(PyGradleGroup grp, String val, String desc) {
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


