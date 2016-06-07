package com.linkedin.gradle.python.util;

import com.linkedin.gradle.python.PythonComponent;

import java.io.File;

public class VirtualEnvExecutableHelper {

    private VirtualEnvExecutableHelper() {

    }

    public static File getPythonInterpreter(PythonComponent pythonComponent) {
        return pythonComponent.getPythonDetails().getVirtualEnvInterpreter();
    }

    public static File getPip(PythonComponent pythonComponent) {
        return getExecutable(pythonComponent, "bin/pip");
    }

    public static File getPex(PythonComponent pythonComponent) {
        return getExecutable(pythonComponent, "bin/pex");
    }

    public static File getExecutable(File file) {
        if(!file.exists()) {
            throw new RuntimeException("Could not find " + file.getAbsolutePath() + " to execute");
        }

        return file;
    }

    public static File getExecutable(PythonComponent pythonComponent, String path) {
        return getExecutable(new File(pythonComponent.getPythonDetails().getVirtualEnv(), path));
    }

    public static File findExecutable(PythonComponent pythonComponent, String path) {
        return new File(pythonComponent.getPythonDetails().getVirtualEnv(), path);
    }
}
