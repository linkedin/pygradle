/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.PythonDetails;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * This class is used to make sure that the up-to-date logic works. It also allows for lazy evaluation
 * of the sources, which comes from the lazy eval of the getPythonExtension(). It's lazy because its a method call
 * and will only get executed right before gradle tries to figure out the inputs/outputs. By making it lazy
 * will allow {@link PythonExtension} to be updated by the project and be complete
 * when its used in the tasks.
 */
abstract public class AbstractPythonInfrastructureDefaultTask extends DefaultTask implements FailureReasonProvider {

    private List<String> arguments = new ArrayList<>();
    // similar to additionalArguments, but not overridable by user's build script
    private PythonExtension pythonExtension;

    private String output;

    @InputFiles
    public FileCollection getSourceFiles() {
        ConfigurableFileTree componentFiles = getProject().fileTree(getPythonExtension().srcDir);
        componentFiles.exclude(standardExcludes());
        return componentFiles;
    }

    public String[] standardExcludes() {
        return new String[]{"**/*.pyc", "**/*.pyo", "**/__pycache__/", "**/*.egg-info/"};
    }

    @Internal
    public PythonExtension getPythonExtension() {
        if (null == pythonExtension) {
            pythonExtension = getProject().getExtensions().getByType(PythonExtension.class);
        }
        return pythonExtension;
    }

    @Internal
    abstract PythonDetails getPythonDetails();

    @Input
    public boolean ignoreExitValue = false;

    public OutputStream stdOut = System.out;

    public OutputStream errOut = System.err;

    public void args(String... args) {
        arguments.addAll(Arrays.asList(args));
    }

    public void args(Collection<String> args) {
        arguments.addAll(args);
    }

    @TaskAction
    public void executePythonProcess() {
        preExecution();

        final TeeOutputContainer container = new TeeOutputContainer(stdOut, errOut);

        ExecResult result = getProject().exec(execSpec -> {
            execSpec.environment(getPythonExtension().pythonEnvironment);
            execSpec.commandLine(getPythonDetails().getVirtualEnvInterpreter());
            // arguments are passed to the python interpreter
            execSpec.args(arguments);
            execSpec.setIgnoreExitValue(ignoreExitValue);

            container.setOutputs(execSpec);

            configureExecution(execSpec);
        });

        output = container.getCommandOutput();

        processResults(result);
    }

    public void configureExecution(ExecSpec spec) {
    }

    public void preExecution() {
    }

    public abstract void processResults(ExecResult execResult);

    @Internal
    @Override
    public String getReason() {
        return output;
    }
}
