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

package com.linkedin.gradle.python.plugin.internal.base;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.PythonTestSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonTestSourceSet;
import com.linkedin.gradle.python.plugin.internal.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultSourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.SourceDistBinarySpecInternal;
import com.linkedin.gradle.python.spec.binary.internal.WheelBinarySpecInternal;
import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultPythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironmentContainer;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.InstallLocalProjectTask;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.AddDependsOnTaskAction;
import com.linkedin.gradle.python.tasks.internal.configuration.CreateVirtualEnvConfigureAction;
import com.linkedin.gradle.python.tasks.internal.configuration.DependencyConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.InstallLocalConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.PyTestConfigurationAction;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.model.*;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.TypeBuilder;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;


public class PythonBaseRulePlugin extends RuleSource {

    public static final String CREATE_VIRTUAL_ENV_TASK = "createVirtualEnv";
    public static final String INSTALL_REQUIRED_DEPENDENCIES_TASK = "installRequiredDependencies";
    public static final String INSTALL_RUNTIME_DEPENDENCIES_TASK = "installRuntimeDependencies";
    public static final String INSTALL_TEST_DEPENDENCIES_TASK = "installTestDependencies";
    public static final String INSTALL_EDITABLE_TASK = "installEditable";

    private static final Logger logger = Logging.getLogger(PythonBaseRulePlugin.class);

    @ComponentType
    public void register(TypeBuilder<PythonComponentSpec> builder) {
        builder.defaultImplementation(DefaultPythonComponentSpec.class);
        builder.internalView(PythonComponentSpecInternal.class);
    }

    @ComponentType
    public void registerWheel(TypeBuilder<WheelBinarySpec> builder) {
        builder.defaultImplementation(DefaultWheelBinarySpec.class);
        builder.internalView(WheelBinarySpecInternal.class);
    }

    @ComponentType
    public void registerSourceDist(TypeBuilder<SourceDistBinarySpec> builder) {
        builder.defaultImplementation(DefaultSourceDistBinarySpec.class);
        builder.internalView(SourceDistBinarySpecInternal.class);
    }


    @ComponentType
    public void registerLanguage(TypeBuilder<PythonSourceSet> builder) {
        builder.defaultImplementation(DefaultPythonSourceSet.class);
    }

    @ComponentType
    public void registerLanguageTests(TypeBuilder<PythonTestSourceSet> builder) {
        builder.defaultImplementation(DefaultPythonTestSourceSet.class);
    }

    @Model
    PythonPluginConfigurations configurations(ExtensionContainer extensions) {
        return extensions.getByType(PythonPluginConfigurations.class);
    }

    @Mutate
    void configurePythonComponents(final ModelMap<PythonComponentSpecInternal> specs, @Path("buildDir") final File buildDir,
                                   final ServiceRegistry serviceRegistry) {
        final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
        specs.beforeEach(new Action<PythonComponentSpecInternal>() {
            @Override
            public void execute(PythonComponentSpecInternal specInternal) {
                specInternal.setBuildDir(buildDir);
                specInternal.setExecActionFactory(execActionFactory);
            }
        });
    }

    @Finalize
    void configurePythonBinariesWithEnvironment(@Each PythonComponentSpecInternal spec) {
        PythonEnvironmentContainer container = spec.getPythonEnvironments();
        for (PythonBinarySpec pythonBinarySpec : spec.getBinaries().withType(PythonBinarySpec.class)) {
            if (StringUtils.isNotBlank(pythonBinarySpec.getTarget())) {
                pythonBinarySpec.setPythonEnvironment(container.getPythonEnvironment(pythonBinarySpec.getTarget()));
            }
        }
    }

    @Validate
    void validateBinaries(@Path("components") final ModelMap<PythonBinarySpec> binarySpecs) {
        for (PythonBinarySpec pythonBinarySpec : binarySpecs.withType(PythonBinarySpec.class)) {
            if (pythonBinarySpec.getPythonEnvironment() == null) {
                throw new GradleException(String.format("%s does not have a defined python environment, please define it", pythonBinarySpec.getName()));
            }
        }
    }

    @Validate
    void validateComponent(@Each PythonComponentSpecInternal pythonComponentSpecInternal) {
        if (pythonComponentSpecInternal.getPythonEnvironments().isEmpty()) {
            throw new GradleException(pythonComponentSpecInternal.getName() + " must have at least 1 targetPlatform");
        }
    }

    @Mutate
    void createVirtalEnvironments(ModelMap<Task> taskContainer, final ModelMap<PythonComponentSpecInternal> specs,
                                  final PythonPluginConfigurations configurations) {
        logger.debug("Creating Virtual Envs");
        for (PythonComponentSpecInternal spec : specs) {
            for (PythonEnvironment pythonEnvironment : spec.getPythonEnvironments().getPythonEnvironments().values()) {

                logger.debug("Executing for {} and {}", spec.getName(), pythonEnvironment.getVersion().getVersionString());

                String taskPostfix = pythonEnvironment.getVersion().getVersionString();

                String createVirtualEnvTask = CREATE_VIRTUAL_ENV_TASK + taskPostfix;
                taskContainer.create(createVirtualEnvTask, VirtualEnvironmentBuild.class,
                        new CreateVirtualEnvConfigureAction(pythonEnvironment, configurations.getBootstrap()));

                final String installRequiredDependencies = INSTALL_REQUIRED_DEPENDENCIES_TASK + taskPostfix;
                taskContainer.create(installRequiredDependencies, InstallDependenciesTask.class,
                        new DependencyConfigurationAction(pythonEnvironment, configurations.getVirtualEnv(), createVirtualEnvTask));

                final String installRuntimeDependencies = INSTALL_RUNTIME_DEPENDENCIES_TASK + taskPostfix;
                taskContainer.create(installRuntimeDependencies, InstallDependenciesTask.class,
                        new DependencyConfigurationAction(pythonEnvironment, configurations.getPython(),
                                installRequiredDependencies));

                final String installTestDependencies = INSTALL_TEST_DEPENDENCIES_TASK + taskPostfix;
                taskContainer.create(installTestDependencies, InstallDependenciesTask.class,
                        new DependencyConfigurationAction(pythonEnvironment, configurations.getPyTest(),
                                installRuntimeDependencies));

                final String installEditable = INSTALL_EDITABLE_TASK + taskPostfix;
                taskContainer.create(installEditable, InstallLocalProjectTask.class,
                        new InstallLocalConfigurationAction(pythonEnvironment, installRuntimeDependencies));

                taskContainer.create(pythonEnvironment.getEnvironmentSetupTaskName(), DefaultTask.class, new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        task.dependsOn(installRequiredDependencies);
                        task.dependsOn(installRuntimeDependencies);
                        task.dependsOn(installTestDependencies);
                        task.dependsOn(installEditable);
                    }
                });
            }
        }
    }

    @Mutate
    void addTestTasks(ModelMap<Task> tasks, final ModelMap<PythonComponentSpecInternal> specs) {
        for (PythonComponentSpecInternal spec : specs) {
            for (PythonEnvironment pythonEnvironment : spec.getPythonEnvironments().getPythonEnvironments().values()) {

                PyTestConfigurationAction configAction = new PyTestConfigurationAction(pythonEnvironment, spec.getSources());

                String taskName = "pyTest" + pythonEnvironment.getVersion().getVersionString();
                tasks.create(taskName, PythonTestTask.class, configAction);
                tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, new AddDependsOnTaskAction(taskName));
            }
        }
    }
}
