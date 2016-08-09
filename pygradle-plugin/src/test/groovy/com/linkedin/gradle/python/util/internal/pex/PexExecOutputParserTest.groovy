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
package com.linkedin.gradle.python.util.internal.pex

import org.gradle.api.GradleException
import spock.lang.Specification


class PexExecOutputParserTest extends Specification {

    def 'can parse output from pex'() {
        given:
        def output = """\
        |Processing /export/home/tester/hudson/data/workspace/MP_TRUNKDEV_MP_DEP/ratchet_895fe7e12c1d15e1204d9fa9c0c05534176c2f12/ratchet
        |Building wheels for collected packages: ratchet
        |  Running setup.py bdist_wheel for ratchet
        |  Stored in directory: /export/home/tester/hudson/data/workspace/MP_TRUNKDEV_MP_DEP/ratchet_895fe7e12c1d15e1204d9fa9c0c05534176c2f12/build/ratchet/wheel-cache
        |Successfully built ratchet
        |Could not satisfy all requirements for pyasn1:
        |    pyasn1, pyasn1==0.1.7(from: oauth2client)
        |
        |NOTE: In order to use the FileCache you must have
        |lockfile installed. You can install it via pip:
        |  pip install lockfile""".stripMargin().stripIndent()

        when:
        def parser = new PexExecOutputParser(output, 1)
        parser.validatePexBuildSuccessfully()

        then:
        def e = thrown(GradleException)
        e.message.startsWith("Failed to build a pex file (see output above)!")
    }
}
