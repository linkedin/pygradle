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
package com.linkedin.gradle.python.extension

import com.linkedin.gradle.python.extension.internal.DefaultPythonDetails
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class DefaultPythonDetailsTest extends Specification {

    def project = new ProjectBuilder().build()
    def details = new DefaultPythonDetails(project, new File("/foo/bar/venv"))

    /* Most of the work is done by the PythonDefaultVersions class, so see
       that class and tests.  We'd like to be able to completely test
       PythonDetails.setPythonVersion() too, but that's currently impossible,
       since that method searches for a Python interpreter on the file system
       matching the selected version.  We can't guarantee that such a Python
       version will exist so we can't test it without perhaps some mocking of
       operatingSystem.findInPath() , which I haven't been able to come up
       with yet.

       It does seem like this one can be tested though.
     */
    def 'test set to unacceptable version'() {
        when:
        details.setPythonDefaultVersions(new PythonDefaultVersions(['2.7', '3.5', '3.6']))
        details.setPythonVersion('2.5')

        then:
        thrown(GradleException)
    }
}
