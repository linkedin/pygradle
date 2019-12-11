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
import com.linkedin.gradle.python.tasks.exec.ExternalExec;
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
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
abstract public class AbstractPythonMainSourceDefaultTask extends DefaultTask implements FailureReasonProvider {

    FileTree sources;
    private List<String> arguments = new ArrayList<>();
    // similar to additionalArguments, but not overridable by user's build script
    private List<String> subArguments = new ArrayList<>();
    private PythonExtension extension;
    private PythonDetails pythonDetails;
    protected String output;

    @Input
    public List<String> additionalArguments = new ArrayList<>();

    ExternalExec externalExec = new ProjectExternalExec(getProject());

    @InputFiles
    public FileCollection getSourceFiles() {
        ConfigurableFileTree componentFiles = getProject().fileTree(getComponent().srcDir);
        componentFiles.exclude(standardExcludes());
        if (null != sources) {
            return sources.plus(componentFiles);
        }
        return componentFiles;
    }

    public String[] standardExcludes() {
        return new String[]{"**/*.pyc", "**/*.pyo", "**/__pycache__/", "**/*.egg-info/"};
    }

    public PythonExtension getPythonExtension() {
        if (null == extension) {
            extension = getProject().getExtensions().getByType(PythonExtension.class);
        }
        return extension;
    }

    /**
     * Use {@link AbstractPythonMainSourceDefaultTask#getPythonExtension()} instead.
     */
    @Internal
    @Deprecated
    public PythonExtension getComponent() {
        return getPythonExtension();
    }

    @Internal
    public PythonDetails getPythonDetails() {
        if (null == pythonDetails) {
            pythonDetails = getComponent().getDetails();
        }
        return pythonDetails;
    }

    public void setPythonDetails(PythonDetails pythonDetails) {
        this.pythonDetails = pythonDetails;
    }

    @InputDirectory
    public FileTree getVirtualEnv() {
        ConfigurableFileTree files = getProject().fileTree(getPythonDetails().getVirtualEnv());
        files.exclude(standardExcludes());
        return files;
    }

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

    public void subArgs(String... args) {
        subArguments.addAll(Arrays.asList(args));
    }

    public void subArgs(Collection<String> args) {
        subArguments.addAll(args);
    }

    @TaskAction
    public void executeTask() {
        preExecution();
        executePythonProcess();
    }

    void executePythonProcess() {

        final TeeOutputContainer container = new TeeOutputContainer(stdOut, errOut);

        ExecResult result = externalExec.exec(execSpec -> {
            execSpec.environment(getComponent().pythonEnvironment);
            execSpec.environment(getComponent().pythonEnvironmentDistgradle);
            execSpec.commandLine(getPythonDetails().getVirtualEnvInterpreter());
            // arguments are passed to the python interpreter
            execSpec.args(arguments);
            // subArguments are arguments for previous arguments. eg: arguments to py.test like -k
            execSpec.args(subArguments);
            // additionalArguments are same as subArguments, but are expected from user's build script
            execSpec.args(additionalArguments);
            execSpec.setIgnoreExitValue(true);

            container.setOutputs(execSpec);

            configureExecution(execSpec);
        });

        output = container.getCommandOutput();

        if (!ignoreExitValue) {
            result.assertNormalExitValue();
        }

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
