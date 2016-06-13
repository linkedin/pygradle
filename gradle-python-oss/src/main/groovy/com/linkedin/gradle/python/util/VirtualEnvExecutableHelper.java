package com.linkedin.gradle.python.util;

import com.linkedin.gradle.python.PythonExtension;

import java.io.File;

public class VirtualEnvExecutableHelper {

    private VirtualEnvExecutableHelper() {

    }

    public static File getPythonInterpreter(PythonExtension pythonExtension) {
        return pythonExtension.getDetails().getVirtualEnvInterpreter();
    }

    public static File getPip(PythonExtension pythonExtension) {
        return getExecutable(pythonExtension, "bin/pip");
    }

    public static File getPex(PythonExtension pythonExtension) {
        return getExecutable(pythonExtension, "bin/pex");
    }

    public static File getExecutable(File file) {
        if(!file.exists()) {
            throw new RuntimeException("Could not find " + file.getAbsolutePath() + " to execute");
        }

        return file;
    }

    public static File getExecutable(PythonExtension pythonExtension, String path) {
        return getExecutable(new File(pythonExtension.getDetails().getVirtualEnv(), path));
    }

    public static File findExecutable(PythonExtension pythonExtension, String path) {
        return new File(pythonExtension.getDetails().getVirtualEnv(), path);
    }
}
