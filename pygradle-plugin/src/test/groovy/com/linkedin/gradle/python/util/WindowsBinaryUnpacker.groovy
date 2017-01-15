package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.extension.PythonDetailsWindowsTest

class WindowsBinaryUnpacker {
    private WindowsBinaryUnpacker() {}

    enum PythonVersion {
        PYTHON_26(2, 6),
        PYTHON_27(2, 7),
        PYTHON_35(3, 5)

        final int major
        final int minor

        PythonVersion(int major, int minor) {
            this.major = major
            this.minor = minor
        }
    }

    public static File buildPythonExec(File destination, PythonVersion version) {
        def exeRoot = PythonDetailsWindowsTest.getResource("/windows/python/shim/python${version.major}${version.minor}.exe")

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
