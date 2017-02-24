package com.linkedin.gradle.python.util.values

/**
 * CodeNarc is complaining that some classes exceed 350 lines.  TO fall into compliance with CodeNarc, the standard values
 * are being moved to this enum.  Its an Enum rather than an interface because according to CodeNarc, interfaces of nothing
 * but constants is now taboo.
 */
enum PyGradleConfiguration {
    BOOTSTRAP_REQS('pygradleBootstrap'),
    SETUP_REQS('setupRequires'),
    BUILD_REQS('build'),
    DEFAULT('default'),
    PYDOCS('pydocs'),
    PYTHON('python'),
    TEST('test'),
    VENV('venv'),
    WHEEL('wheel'),

    private final String value

    PyGradleConfiguration(String val) {
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
