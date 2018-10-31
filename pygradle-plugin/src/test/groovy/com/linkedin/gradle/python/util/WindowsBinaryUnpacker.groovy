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
package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.extension.DefaultPythonDetailsWindowsTest

class WindowsBinaryUnpacker {
    private WindowsBinaryUnpacker() { }

    enum PythonVersion {
        PYTHON_27(2, 7),
        PYTHON_35(3, 5),
        PYTHON_36(3, 6),
        PYTHON_37(3, 7),

        final int major
        final int minor

        PythonVersion(int major, int minor) {
            this.major = major
            this.minor = minor
        }
    }

    public static File buildPythonExec(File destination, PythonVersion version) {
        def exeRoot = DefaultPythonDetailsWindowsTest.getResource("/windows/python/shim/python${version.major}${version.minor}.exe")

        new File(destination, "python${version.major}.${version.minor}.exe").withOutputStream { out ->
            out << exeRoot.openStream()
        }

        new File(destination, "python${version.major}.exe").withOutputStream { out ->
            out << exeRoot.openStream()
        }

        new File(destination, "python.exe").withOutputStream { out ->
            out << exeRoot.openStream()
        }

        return destination
    }
}
