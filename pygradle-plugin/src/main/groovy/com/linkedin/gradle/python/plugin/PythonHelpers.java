package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.util.ColorHelper;
import com.linkedin.gradle.python.util.ConsoleOutput;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Project;

public class PythonHelpers {

    private PythonHelpers() {
        //NOOP
    }

    public static final int LINE_WIDTH = 80;

    /**
     * Detect if we're connected to a TTY and support color.
     * <p>
     *
     * @param project The project to use to invoke the command.
     * @return true if we're connected to a TTY and support color.
     */
    protected static boolean isTty(Project project) {
        return project.getGradle().getStartParameter().getConsoleOutput() != null;
    }

    /**
     * Return a string that indicates success!
     * <p>
     * If not connected to a TTY that supports color, then an ASCII success
     * message will be returned.
     * <p>
     *
     * @param project The project running this method.
     * @return A string indicating success!
     */
    public static String successFlair(Project project, PythonExtension settings) {

        StringBuilder successFlair = new StringBuilder();

        if (!settings.consoleOutput.equals(ConsoleOutput.RAW)) {
            StringGroovyMethods.leftShift(successFlair, ColorHelper.ANSI_GREEN);
        }


        if (isTty(project)) {
            StringGroovyMethods.leftShift(successFlair, "\u2713");
        } else {
            StringGroovyMethods.leftShift(successFlair, " [GOOD]");
        }


        if (!settings.consoleOutput.equals(ConsoleOutput.RAW)) {
            StringGroovyMethods.leftShift(successFlair, ColorHelper.ANSI_RESET);
        }


        return successFlair.toString();
    }

    public static String createPrettyLine(String prefix, String postfix) {
        StringBuilder successFlair = new StringBuilder();


        successFlair.append(prefix).append(" ");
        for (int i = 0; i < LINE_WIDTH - 3 - prefix.length() - postfix.length(); i++) {
            successFlair.append(".");
        }

        successFlair.append(".");
        successFlair.append(" ").append(postfix);

        return successFlair.toString();

    }
}
