package com.linkedin.gradle.python

/**
 * Enums used to specify which cfg2 generation to use when generating an
 * app-def for the Python application.
 * <p>
 * More information can be found in the {@link LiPythonAppDef} plugin.
 */
enum Cfg2Generation {

    /**
     * Generate app-defs using the next-gen mechanism
     */
    OLD_GEN,

    /**
     * Generate app-defs using the old-style mechanism
     */
    NEXT_GEN
}
