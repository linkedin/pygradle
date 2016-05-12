package com.linkedin.gradle.python.plugin


import spock.lang.Specification

class PythonHelpersTest extends Specification {

    def 'PythonHelpers.compareVersions works properly'() {
        expect: "Comparing two versions"
        PythonHelpers.compareVersions('', '') == 0
        PythonHelpers.compareVersions('0.3.63', '0.3.80') == -1
        PythonHelpers.compareVersions('0.3.80', '0.3.80') == 0
        PythonHelpers.compareVersions('0.3.81', '0.3.80') == 1
        PythonHelpers.compareVersions('0.3.101', '0.3.80') == 1
        PythonHelpers.compareVersions('0.3', '0.3.80') == -1
        PythonHelpers.compareVersions('0.4', '0.3.80') == 1
        PythonHelpers.compareVersions('0.3.70.1', '0.3.80') == -1
        PythonHelpers.compareVersions('0.3.80.1', '0.3.80') == 1
        PythonHelpers.compareVersions('0.3.81.1', '0.3.80') == 1
        PythonHelpers.compareVersions('0.3.80-SNAPSHOT', '0.3.80') == 1
    }
}
