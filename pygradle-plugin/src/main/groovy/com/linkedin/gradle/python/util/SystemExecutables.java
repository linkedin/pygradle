package com.linkedin.gradle.python.util;

import org.apache.tools.ant.taskdefs.condition.Os;

/**
 * Enum that allows for platform independent resolution of executables
 */
public enum SystemExecutables {
    PYTHON("python", "exe", null),
    PYTHON_VERSION_AWARE("python%s", "exe", null),
    ACTIVATE("activate", "bat", null),
    TEST_FILE("example", "bat", "sh");

    private String exeName;
    private String winExtension;
    private String nixExtension;

    SystemExecutables(String exeName, String winExtension, String nixExtension) {
        this.exeName = exeName;
        this.winExtension = winExtension;
        this.nixExtension = nixExtension;
    }

    public String getExecutable() {

        String exeNameReturn = exeName;

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            exeNameReturn = exeName + "." +  winExtension;
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            if (nixExtension != null) {
                exeNameReturn = exeName + "." + nixExtension;
            } else {
                exeNameReturn = exeName;
            }
        }

        return exeNameReturn;
    }

    public String getExecutable(String versionAware) {
        String exeNameReturn = getExecutable();
        return String.format(exeNameReturn, versionAware);
    }
}
