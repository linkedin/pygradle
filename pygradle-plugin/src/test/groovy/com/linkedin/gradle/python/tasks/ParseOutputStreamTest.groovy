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
package com.linkedin.gradle.python.tasks


import spock.lang.Specification

class ParseOutputStreamTest extends Specification {

  def 'parse example stream'() {
    setup:
    def stream = '''============================= test session starts ==============================
platform darwin -- Python 2.6.9 -- py-1.4.26 -- pytest-2.7.0
rootdir: /Users/ethall/workspace/lipy-products/lipy-mint_trunk/lipy-mint, inifile: setup.cfg
plugins: cov, mock, xdist
collected 3 items

test/test_add_to_artifact_spec.py ...Coverage.py warning: No data was collected.

--------------- coverage: platform darwin, python 2.6.9-final-0 ----------------
Name                                                Stmts   Miss  Cover
-----------------------------------------------------------------------
src/__init__                                            1      1     0%
src/linkedin/__init__                                   6      6     0%
src/linkedin/mint/__init__                              6      6     0%
src/linkedin/mint/add_to_artifact_spec                 73     73     0%
src/linkedin/mint/branch_commands/__init__              6      6     0%
src/linkedin/mint/branch_commands/create              264    264     0%
src/linkedin/mint/branch_commands/delete               80     80     0%
src/linkedin/mint/branch_commands/list                 13     13     0%
src/linkedin/mint/build                               164    164     0%
src/linkedin/mint/buildspec_dependencies              192    192     0%
src/linkedin/mint/consistent_versions                 266    266     0%
src/linkedin/mint/consistent_versions_from_gradle       6      6     0%
src/linkedin/mint/constants                           100    100     0%
src/linkedin/mint/create_empty_specs                   26     26     0%
src/linkedin/mint/dependencies                        169    169     0%
src/linkedin/mint/dependency_graph                     78     78     0%
src/linkedin/mint/dependency_report                   119    119     0%
src/linkedin/mint/find_artifacts                       45     45     0%
src/linkedin/mint/integfwk/__init__                     6      6     0%
src/linkedin/mint/integfwk/config                     267    267     0%
src/linkedin/mint/integfwk/services                   386    386     0%
src/linkedin/mint/ivy_dependencies                    139    139     0%
src/linkedin/mint/main                                192    192     0%
src/linkedin/mint/mint_cfg                            307    307     0%
src/linkedin/mint/mint_deployment                     123    123     0%
src/linkedin/mint/mint_integration                    112    112     0%
src/linkedin/mint/mintintegration_network_tools       467    467     0%
src/linkedin/mint/mintintegration_tools                98     98     0%
src/linkedin/mint/mplint                              260    260     0%
src/linkedin/mint/new_product                         587    587     0%
src/linkedin/mint/parse_productspec_deps               44     44     0%
src/linkedin/mint/product                              85     85     0%
src/linkedin/mint/product_commands/__init__             6      6     0%
src/linkedin/mint/product_commands/blacklist           76     76     0%
src/linkedin/mint/product_commands/common             383    383     0%
src/linkedin/mint/product_commands/delete             145    145     0%
src/linkedin/mint/product_commands/describe            83     83     0%
src/linkedin/mint/product_commands/edit               223    223     0%
src/linkedin/mint/product_commands/list                39     39     0%
src/linkedin/mint/product_commands/lock                42     42     0%
src/linkedin/mint/publish                             220    220     0%
src/linkedin/mint/review                               37     37     0%
src/linkedin/mint/scm                                 317    317     0%
src/linkedin/mint/staged_commit                        31     31     0%
src/linkedin/mint/use_artifact                        176    176     0%
src/linkedin/mint/util                                 64     64     0%
src/linkedin/mint/validate                            482    482     0%
src/linkedin/mint/version_checker                      85     85     0%
-----------------------------------------------------------------------
TOTAL                                                7096   7096     0%
Coverage HTML written to dir htmlcov/foo /sdfasdf
Coverage XML written to file coverage.xml

=========================== 3 passed in 2.73 seconds ==========================='''

    when:
    def parseOutputStream = new PyCoverageTask.ParseOutputStream()
    parseOutputStream.processStream(stream)

    then:
    parseOutputStream.coverageXml == 'coverage.xml'
    parseOutputStream.htmlDir == 'htmlcov/foo /sdfasdf'  // This space was intentional
  }
}
