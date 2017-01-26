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
package com.linkedin.gradle.python.plugin

import org.junit.rules.TemporaryFolder

import java.nio.file.Paths

class DefaultProjectLayoutRule extends TemporaryFolder {

    public File buildFile

    protected void before() throws Throwable {
        super.before()
        setupProject()
        buildFile = newFile(Paths.get('foo', "build.gradle").toString())
    }

    @SuppressWarnings("UnnecessaryOverridingMethod") //docs for testing
    protected void after() {
        //It's useful to comment this out if you need to look at the test env
        super.after()
    }

    private void setupProject() {
        newFolder('foo', 'src', 'foo')
        newFolder('foo', 'test')

        // Create some code
        newFile(Paths.get('foo', 'src', 'foo', '__init__.py').toString())
        newFile(Paths.get('foo', 'src', 'foo', 'hello.py').toString()) << '''\
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
        newFile(Paths.get('foo', 'setup.py').toString()) << PyGradleTestBuilder.createSetupPy()

        // Create the setup.cfg file
        newFile(Paths.get('foo', 'setup.cfg').toString()) << PyGradleTestBuilder.createSetupCfg()

        // Create the test directory and a simple test
        newFile(Paths.get('foo', 'test', 'test_a.py').toString()) << '''\
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

        newFile(Paths.get('foo', "MANIFEST.in").toString()) << ''
    }
}
