package com.linkedin.gradle.python.plugin


import com.linkedin.gradle.python.PythonExtension
import com.linkedin.gradle.python.util.ColorHelper
import com.linkedin.gradle.python.util.ConsoleOutput
import org.gradle.api.Project

abstract class PythonHelpers {

    /**
     * Detect if we're connected to a TTY and support color.
     * <p>
     * @param project The project to use to invoke the command.
     * @return true if we're connected to a TTY and support color.
     */
    protected static boolean isTty(Project project) {
        return project.gradle.startParameter.consoleOutput
    }

    /**
     * Return a string that indicates success!
     * <p>
     * If not connected to a TTY that supports color, then an ASCII success
     * message will be returned.
     * <p>
     * @param project The project running this method.
     * @return A string indicating success!
     */
    public static String successFlair(Project project, PythonExtension settings) {

        StringBuilder successFlair = new StringBuilder()

        if (settings.consoleOutput != ConsoleOutput.RAW)
            successFlair << ColorHelper.ANSI_GREEN

        if (isTty(project))
            successFlair << "\u2713"
        else
            successFlair << ' [GOOD]'

        if (settings.consoleOutput != ConsoleOutput.RAW)
            successFlair << ColorHelper.ANSI_RESET

        return successFlair.toString()
    }

    /**
     * Returns operating system and architecture pair.
     * <p>
     * The operating system and architecture pair is joined with an "_"
     * character in compliance with the multi-variant build system. Read more
     * about the multi-variant build system at http://go/mbf.
     * @return The operating system and architecture pair.
     */
    protected static String getOsNameAndArch() {
        def osName = System.getProperty('os.name').toLowerCase()
        if (osName == 'mac os x')
            osName = 'darwin'
        def osArch = System.getProperty('os.arch')
        if (osArch == 'amd64') {
            osArch = 'x86_64'
        }
        return "${osName}_${osArch}";
    }
}

