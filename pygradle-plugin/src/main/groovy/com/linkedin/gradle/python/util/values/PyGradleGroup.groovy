package com.linkedin.gradle.python.util.values

/**
 * Created by scphantm on 2/23/17.
 */
enum PyGradleGroup {
    DOCUMENTATION('documentation'),
    BUILD('build'),
    HELP('help'),
    VERIFICATION('verification'),
    PYGRADLE('pygradle tasks'),
    PYGRADLE_VENV('pygradle virtual environment')

    private final String value

    PyGradleGroup(String val) {
        this.value = val
    }

    @Override
    String toString() {
        return value
    }

    String getValue() {
        return value
    }
}
