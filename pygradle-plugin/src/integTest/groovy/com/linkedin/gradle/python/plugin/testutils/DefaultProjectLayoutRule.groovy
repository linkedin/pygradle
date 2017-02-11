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
package com.linkedin.gradle.python.plugin.testutils

import org.apache.tools.ant.taskdefs.condition.Os
import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder

import java.nio.file.Paths

class DefaultProjectLayoutRule extends ExternalResource implements ProjectLayoutRule {
    TemporaryFolder tempFolder

    public static final String PROJECT_NAME_DIR = "foo"

    public DefaultProjectLayoutRule() {
        tempFolder = new TemporaryFolder(buildCreateWinTemp())
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private File buildCreateWinTemp() {
        // GRADLE_TEST_TEMP_FOLDER
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            File tmpFldr = new File("c:/tmp")
            if (!tmpFldr.exists()) {
                tmpFldr.mkdirs()
            }
            return tmpFldr
        }
        return null
    }

    public File buildFile

    @Override
    void before() throws Throwable {
        tempFolder.before()
        newFolder(PROJECT_NAME_DIR)
        setupProject()
        buildFile = newFile(Paths.get(PROJECT_NAME_DIR, "build.gradle").toString())
    }

    @Override
    @SuppressWarnings("UnnecessaryOverridingMethod")
    void after() {
        //It's useful to comment this out if you need to look at the test env
        tempFolder.after()
    }

    @Override
    void create() throws IOException {
        tempFolder.create()
    }

    @Override
    File newFile(String fileName) throws IOException {
        tempFolder.newFile(fileName)
    }

    @Override
    File newFile() throws IOException {
        tempFolder.newFile()
    }

    @Override
    File newFolder(String folder) throws IOException {
        tempFolder.newFolder(folder)
    }

    @Override
    File newFolder(String... folderNames) throws IOException {
        tempFolder.newFolder(folderNames)
    }

    @Override
    File newFolder() throws IOException {
        tempFolder.newFolder()
    }

    @Override
    File getRoot() {
        tempFolder.getRoot()
    }

    @Override
    void delete() {
        tempFolder.delete()
    }

    void setupProject() {
        newFolder(PROJECT_NAME_DIR, 'src', PROJECT_NAME_DIR)
        newFolder(PROJECT_NAME_DIR, 'test')

        // Create some code
        newFile(Paths.get(PROJECT_NAME_DIR, 'src', PROJECT_NAME_DIR, '__init__.py').toString())
        newFile(Paths.get(PROJECT_NAME_DIR, 'src', PROJECT_NAME_DIR, 'hello.py').toString()) << '''\
        | from __future__ import print_function
        |
        |
        | def generate_welcome():
        |     return 'Hello World'
        |
        |
        | def main():
        |     print(generate_welcome())
        |'''.stripMargin().stripIndent()

        // set up the default project name
        def settingsGradle = newFile('settings.gradle')
        settingsGradle << PyGradleTestBuilder.createSettingGradle()
        settingsGradle << "\ninclude ':foo'\n"

        // Create a setup file
        newFile(Paths.get(PROJECT_NAME_DIR, 'setup.py').toString()) << PyGradleTestBuilder.createSetupPy()

        // Create the setup.cfg file
        newFile(Paths.get(PROJECT_NAME_DIR, 'setup.cfg').toString()) << PyGradleTestBuilder.createSetupCfg()

        // Create the test directory and a simple test
        newFile(Paths.get(PROJECT_NAME_DIR, 'test', 'test_a.py').toString()) << '''\
            | from foo.hello import generate_welcome
            |
            |
            | def test_sanity():
            |     expected = 6
            |     assert 2 * 3 == expected
            |
            |
            | def test_calling_method():
            |     assert generate_welcome() == 'Hello World'
            '''.stripMargin().stripIndent()

        newFile(Paths.get(PROJECT_NAME_DIR, "MANIFEST.in").toString()) << ''
    }
}
