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

import spock.lang.Specification


class PipFreezeOutputParserTest extends Specification {

    def 'can parse pip freeze output'() {
        def freezeOutput = '''\
            |alabaster==0.7.1
            |Babel==1.3
            |colorama==0.3.7
            |coverage==3.7.1
            |docutils==0.11
            |flake8==2.5.4
            |imagesize==0.7.1
            |Jinja2==2.3
            |mccabe==0.2.1
            |pbr==1.8.0
            |pep8==1.5.7
            |pex==1.2.2
            |py==1.4.29
            |pyflakes==0.8.1
            |Pygments==2.0
            |pytest==2.9.1
            |pytest-cov==2.2.1
            |pytest-xdist==1.14
            |pytz==2016.4
            |setuptools-git==1.1
            |six==1.10.0
            |snowballstemmer==1.1.0
            |Sphinx==1.4.1
            |testProject===unspecified
            |wheel==0.26.0'''.stripMargin().stripIndent()

        expect:
        PipFreezeOutputParser.getDependencies([
            'pbr', 'Babel', 'pep8', 'py', 'setuptools', 'pytest-xdist', 'Jinja2', 'flake8', 'snowballstemmer',
            'alabaster', 'sphinx_rtd_theme', 'Pygments', 'pytest-cov', 'pip', 'mccabe', 'docutils', 'coverage', 'pex',
            'six', 'setuptools-git', 'pyflakes', 'pytest', 'wheel', 'imagesize', 'argparse', 'Sphinx', 'colorama',
            'pytz'
        ], freezeOutput) == ['testProject']
    }
}
