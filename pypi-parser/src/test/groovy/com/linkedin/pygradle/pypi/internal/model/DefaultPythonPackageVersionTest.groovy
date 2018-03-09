package com.linkedin.pygradle.pypi.internal.model

import spock.lang.Specification
import spock.lang.Unroll


class DefaultPythonPackageVersionTest extends Specification {

    @Unroll
    def 'can parse example #version'() {
        when:
        def pythonVersion = new DefaultPythonPackageVersion(version)

        then:
        noExceptionThrown()
        pythonVersion.release == release
        pythonVersion.epoch == epoch
        pythonVersion.post == post
        pythonVersion.pre == pre
        pythonVersion.dev == dev
        pythonVersion.local == local
        pythonVersion.toVersionString() == version
        pythonVersion.isWildcardVersion() == wildcard

        where:
        version                | release   | epoch | pre   | post  | dev   | local   | wildcard
        "0.9"                  | "0.9"     | null  | null  | null  | null  | null    | false
        "0.9.1"                | "0.9.1"   | null  | null  | null  | null  | null    | false
        "0.9.2"                | "0.9.2"   | null  | null  | null  | null  | null    | false
        "0.9.10"               | "0.9.10"  | null  | null  | null  | null  | null    | false
        "0.9.11"               | "0.9.11"  | null  | null  | null  | null  | null    | false
        "1.0"                  | "1.0"     | null  | null  | null  | null  | null    | false
        "1.0.1"                | "1.0.1"   | null  | null  | null  | null  | null    | false
        "1.1"                  | "1.1"     | null  | null  | null  | null  | null    | false
        "2.0"                  | "2.0"     | null  | null  | null  | null  | null    | false
        "2.0.1"                | "2.0.1"   | null  | null  | null  | null  | null    | false
        "2012.04"              | "2012.04" | null  | null  | null  | null  | null    | false
        "2012.07"              | "2012.07" | null  | null  | null  | null  | null    | false
        "2012.10"              | "2012.10" | null  | null  | null  | null  | null    | false
        "2013.01"              | "2013.01" | null  | null  | null  | null  | null    | false
        "2013.06"              | "2013.06" | null  | null  | null  | null  | null    | false
        "1!1.0"                | "1.0"     | '1'   | null  | null  | null  | null    | false
        "1!1.1"                | "1.1"     | '1'   | null  | null  | null  | null    | false
        "1!2.0"                | "2.0"     | '1'   | null  | null  | null  | null    | false
        "1.0a1"                | "1.0"     | null  | 'a1'  | null  | null  | null    | false
        "1.0a"                 | "1.0"     | null  | 'a0'  | null  | null  | null    | false
        "1.0a2"                | "1.0"     | null  | 'a2'  | null  | null  | null    | false
        "1.0b1"                | "1.0"     | null  | 'b1'  | null  | null  | null    | false
        "1.0rc1"               | "1.0"     | null  | 'rc1' | null  | null  | null    | false
        "1.0.dev1"             | "1.0"     | null  | null  | null  | '1'   | null    | false
        "1.0.dev2"             | "1.0"     | null  | null  | null  | '2'   | null    | false
        "1.0.dev3"             | "1.0"     | null  | null  | null  | '3'   | null    | false
        "1.0.dev4"             | "1.0"     | null  | null  | null  | '4'   | null    | false
        "1.0c1"                | "1.0"     | null  | 'c1'  | null  | null  | null    | false
        "1.0c2"                | "1.0"     | null  | 'c2'  | null  | null  | null    | false
        "1.0.post1"            | "1.0"     | null  | null  | '1'   | null  | null    | false
        "1.1.dev1"             | "1.1"     | null  | null  | null  | '1'   | null    | false
        "1.0.dev456"           | "1.0"     | null  | null  | null  | '456' | null    | false
        "1.0a1"                | "1.0"     | null  | 'a1'  | null  | null  | null    | false
        "1.0a2.dev456"         | "1.0"     | null  | 'a2'  | null  | '456' | null    | false
        "1.0a12.dev456"        | "1.0"     | null  | 'a12' | null  | '456' | null    | false
        "1.0a12"               | "1.0"     | null  | 'a12' | null  | null  | null    | false
        "1.0b1.dev456"         | "1.0"     | null  | 'b1'  | null  | '456' | null    | false
        "1.0b2"                | "1.0"     | null  | 'b2'  | null  | null  | null    | false
        "1.0b2.post345.dev456" | "1.0"     | null  | 'b2'  | '345' | '456' | null    | false
        "1.0b2.post345"        | "1.0"     | null  | 'b2'  | '345' | null  | null    | false
        "1.0rc1.dev456"        | "1.0"     | null  | 'rc1' | null  | '456' | null    | false
        "1.0rc1"               | "1.0"     | null  | 'rc1' | null  | null  | null    | false
        "1.0+abc.5"            | "1.0"     | null  | null  | null  | null  | 'abc.5' | false
        "1.0+abc.7"            | "1.0"     | null  | null  | null  | null  | 'abc.7' | false
        "1.0+5"                | "1.0"     | null  | null  | null  | null  | '5'     | false
        "1.0.post456.dev34"    | "1.0"     | null  | null  | '456' | '34'  | null    | false
        "1.0.post456"          | "1.0"     | null  | null  | '456' | null  | null    | false
        "1.1.*"                | "1.1"     | null  | null  | null  | null  | null    | true
    }
}
