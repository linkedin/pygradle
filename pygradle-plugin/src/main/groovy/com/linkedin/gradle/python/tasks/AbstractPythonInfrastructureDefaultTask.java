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
import com.linkedin.gradle.python.plugin.PythonHelpers;
import com.linkedin.gradle.python.tasks.action.CreateVirtualEnvAction;
import com.linkedin.gradle.python.tasks.action.VirtualEnvCustomizer;
import com.linkedin.gradle.python.tasks.action.pip.PipInstallAction;
import com.linkedin.gradle.python.tasks.exec.ProjectExternalExec;
import com.linkedin.gradle.python.tasks.execution.FailureReasonProvider;
import com.linkedin.gradle.python.tasks.execution.TeeOutputContainer;
import com.linkedin.gradle.python.tasks.provides.ProvidesVenv;
import com.linkedin.gradle.python.tasks.supports.SupportsDistutilsCfg;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageFiltering;
import com.linkedin.gradle.python.tasks.supports.SupportsPackageInfoSettings;
import com.linkedin.gradle.python.tasks.supports.SupportsWheelCache;
import com.linkedin.gradle.python.util.DefaultEnvironmentMerger;
import com.linkedin.gradle.python.util.DependencyOrder;
import com.linkedin.gradle.python.util.EnvironmentMerger;
import com.linkedin.gradle.python.util.PackageInfo;
import com.linkedin.gradle.python.util.PackageSettings;
import com.linkedin.gradle.python.wheel.EditablePythonAbiContainer;
import com.linkedin.gradle.python.wheel.EmptyWheelCache;
import com.linkedin.gradle.python.wheel.WheelCache;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import javax.annotation.Nullable;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.linkedin.gradle.python.util.StandardTextValues.CONFIGURATION_SETUP_REQS;


/**
 * This class is used to make sure that the up-to-date logic works. It also allows for lazy evaluation
 * of the sources, which comes from the lazy eval of the getPythonExtension(). It's lazy because its a method call
 * and will only get executed right before gradle tries to figure out the inputs/outputs. By making it lazy
 * will allow {@link PythonExtension} to be updated by the project and be complete
 * when its used in the tasks.
 */
abstract public class AbstractPythonInfrastructureDefaultTask extends DefaultTask implements FailureReasonProvider,
    SupportsPackageInfoSettings, SupportsDistutilsCfg, SupportsPackageFiltering, SupportsWheelCache, ProvidesVenv {

    private static final Logger log = Logging.getLogger(AbstractPythonInfrastructureDefaultTask.class);

    private List<String> arguments = new ArrayList<>();
    private PythonExtension pythonExtension;
    private String output;

    @Input
    @Optional
    private String distutilsCfg;
    private PackageSettings<PackageInfo> packageSettings;
    private Spec<PackageInfo> packageExcludeFilter;
    private WheelCache wheelCache = new EmptyWheelCache();
    private EditablePythonAbiContainer editablePythonAbiContainer;

    private EnvironmentMerger environmentMerger = new DefaultEnvironmentMerger();

    @Input
    public boolean ignoreExitValue = false;

    public OutputStream stdOut = System.out;

    public OutputStream errOut = System.err;

    @Input
    abstract protected File getVenvPath();

    @InputFiles
    abstract protected Configuration getInstallConfiguration();

    abstract protected boolean isVenvReady();

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
    abstract public PythonDetails getPythonDetails();

    public void args(String... args) {
        arguments.addAll(Arrays.asList(args));
    }

    public void args(Collection<String> args) {
        arguments.addAll(args);
    }

    @TaskAction
    public void executePythonProcess() {
        if (!isVenvReady()) {
            prepairVenv();
        }

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

    protected void prepairVenv() {
        // We don't know what happened to this venv, so lets kill it
        PythonDetails pythonDetails = getPythonDetails();
        if (pythonDetails.getVirtualEnv().exists()) {
            FileUtils.deleteQuietly(pythonDetails.getVirtualEnv());
        }

        ProjectExternalExec externalExec = new ProjectExternalExec(getProject());

        CreateVirtualEnvAction action = new CreateVirtualEnvAction(getProject(), pythonDetails, editablePythonAbiContainer);
        action.buildVenv(new VirtualEnvCustomizer(distutilsCfg, externalExec, pythonDetails));

        PipInstallAction pipInstallAction = new PipInstallAction(packageSettings, getProject(),
            externalExec, getPythonExtension().pythonEnvironment,
            pythonDetails, wheelCache, environmentMerger);

        installPackages(pipInstallAction, getProject().getConfigurations().getByName(CONFIGURATION_SETUP_REQS.getValue()));
        installPackages(pipInstallAction, getInstallConfiguration());
    }

    private void installPackages(PipInstallAction pipInstallAction, Configuration configuration) {
        DependencyOrder.configurationPostOrderFiles(configuration).forEach(file -> {
            PackageInfo packageInfo = PackageInfo.fromPath(file);
            if (packageExcludeFilter != null && packageExcludeFilter.isSatisfiedBy(packageInfo)) {
                if (PythonHelpers.isPlainOrVerbose(getProject())) {
                    log.lifecycle("Skipping {} - Excluded", packageInfo.toShortHand());
                }
            } else {
                pipInstallAction.installPackage(packageInfo, Collections.emptyList());
            }
        });
    }

    public void configureExecution(ExecSpec spec) {
        // Here for users to override with other impl
    }

    public void preExecution() {
        // Here for users to override with other impl
    }

    public abstract void processResults(ExecResult execResult);

    @Override
    public void setPackageSettings(PackageSettings<PackageInfo> settings) {
        packageSettings = settings;
    }

    @Override
    public PackageSettings<PackageInfo> getPackageSettings() {
        return packageSettings;
    }

    @Override
    public void setDistutilsCfg(String cfg) {
        distutilsCfg = cfg;
    }

    @Override
    public String getDistutilsCfg() {
        return distutilsCfg;
    }

    @Nullable
    @Override
    public Spec<PackageInfo> getPackageExcludeFilter() {
        return packageExcludeFilter;
    }

    @Override
    public void setPackageExcludeFilter(Spec<PackageInfo> filter) {
        packageExcludeFilter = filter;
    }

    @Internal
    @Override
    public String getReason() {
        return output;
    }

    @Override
    public WheelCache getWheelCache() {
        return wheelCache;
    }

    @Override
    public void setWheelCache(WheelCache wheelCache) {
        this.wheelCache = wheelCache;
    }

    @Override
    public void setEditablePythonAbiContainer(EditablePythonAbiContainer editablePythonAbiContainer) {
        this.editablePythonAbiContainer = editablePythonAbiContainer;
    }
}
