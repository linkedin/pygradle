package com.linkedin.gradle.python.util

import com.linkedin.gradle.python.PythonComponent
import com.linkedin.gradle.python.extension.CliExtension
import com.linkedin.gradle.python.extension.PexExtension
import com.linkedin.gradle.python.plugin.PythonPexDistributionPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

public class EntryPointHelpers {

    /**
     * Write an thin pex entry point script.
     * <p>
     * An entry point script is also referred to as a wrapper script. This script simply wraps instruments a
     * call to pex with an entry point.
     * <p>
     * An entry point script includes LID specific environment variables. For example, the <code>BASEDIR</code>
     * is used to calculate the base directory at which to unpack the pex.
     *
     * TODO: Make the template configurable.
     * @param project The project to run <code>pex</code> within.
     * @param path The path at which to create the wrapper script.
     * @param entryPoint The entry point to use in the wrapper script.
     */
    public static void writeEntryPointScript(Project project, String path, String entryPoint) {
        PythonComponent settings = project.getExtensions().getByType(PythonComponent)
        def extensions = ((ExtensionAware) settings).getExtensions()
        PexExtension pexExtension = extensions.getByType(PexExtension)

        boolean isCliTool = extensions.findByType(CliExtension) != null
        def file = new File(path)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        if (isCliTool) {
            file.setExecutable(true, false)
            file.setReadable(true, false)
        } else {
            file.setExecutable(true)
        }
        if (isCliTool && pexExtension.pythonWrapper) {
            def pythonWrapperTemplate = PythonPexDistributionPlugin.getResource('/templates/pex_entrypoint.py.template').text
            file << String.format(pythonWrapperTemplate, "${project.name}.pex", entryPoint)
        } else {
            file << """#!/bin/bash\n"""
            file << """[ -z "\$BASEDIR" ] && BASEDIR=\$( cd "\$( dirname "\${BASH_SOURCE[0]}" )/.." && pwd )\n"""
            file << """exec /usr/bin/env PEX_ROOT="\$BASEDIR/libexec" PEX_MODULE="${entryPoint}" """
            file << """\$BASEDIR/bin/${project.name}.pex "\$@"\n"""
        }
    }

    /**
     * Run ``setup.py entrypoints`` and return the results.
     *
     * The ``entrypoints`` setuptools command is a custom command provided by the distgradle.GradleDistribution
     * distribution class. Only classes that use this distribution class support this command.
     *
     * @param project The project to run ``setup.py entrypoints`` within.
     * @return A list of setuptools entry point strings that looks like ['foo = module1.module2:main', ...].
     */
    static public List<String> collectEntryPoints(Project project) {
        PythonComponent settings = project.getExtensions().getByType(PythonComponent)
        def entryPointsBuf = new ByteArrayOutputStream()
        project.exec {
            environment settings.pythonEnvironment + settings.pythonEnvironmentDistgradle
            commandLine([
                    VirtualEnvExecutableHelper.getPythonInterpreter(settings),
                    'setup.py',
                    'entrypoints',
            ])
            standardOutput entryPointsBuf
        }
        def entryPoints = []
        entryPointsBuf.toString().split('\n').each {
            if (it != 'running entrypoints')
                entryPoints << it
        }
        return entryPoints
    }
}
