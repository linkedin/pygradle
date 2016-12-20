package com.linkedin.gradle.python.util;

import org.apache.tools.ant.taskdefs.condition.Os;

/**
 * Enum that allows for platform independent resolution of executables
 */
public enum SystemExecutables {
    PYTHON("python", "exe"),
    PYTHON_VERSION_AWARE("python%s", "exe"),
    ACTIVATE("activate", "bat");

    private String exeName;
    private String winExtension;

    SystemExecutables(String exeName, String winExtension) {
        this.exeName = exeName;
        this.winExtension = winExtension;
    }

    public String getExecutable() {

        String exeNameReturn = exeName;

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            exeNameReturn = exeName + "." +  winExtension;
        }

        return exeNameReturn;
    }

    public String getExecutable(String versionAware) {
        String exeNameReturn = getExecutable();
        return String.format(exeNameReturn, versionAware);
    }
}
