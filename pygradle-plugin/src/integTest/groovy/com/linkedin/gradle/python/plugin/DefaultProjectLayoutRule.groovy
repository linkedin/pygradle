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

class DefaultProjectLayoutRule extends TemporaryFolder {

    public File buildFile

    protected void before() throws Throwable {
        super.before()
        setupProject()
        buildFile = newFile("build.gradle")
    }

    @SuppressWarnings("UnnecessaryOverridingMethod") //docs for testing
    protected void after() {
        //It's useful to comment this out if you need to look at the test env
        super.after()
    }

    private void setupProject() {
        newFolder('src', 'foo')
        newFolder('test')

        // Create some code
        newFile('src/foo/__init__.py')
        newFile('src/foo/hello.py') << '''\
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
        newFile('settings.gradle') << PyGradleTestBuilder.createSettingGradle()

        // Create a setup file
        newFile('setup.py') << PyGradleTestBuilder.createSetupPy()

        // Create the setup.cfg file
        newFile('setup.cfg') << PyGradleTestBuilder.createSetupCfg()

        // Create the test directory and a simple test
        newFile('test/test_a.py') << '''\
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

        newFile("MANIFEST.in") << ''
    }
}
