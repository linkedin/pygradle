package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.InstallLocalProjectTask;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.configuration.CreateVirtualEnvConfigureAction;
import com.linkedin.gradle.python.tasks.internal.configuration.DependencyConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.InstallLocalConfigurationAction;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryTasksCollection;

import java.io.File;


@SuppressWarnings("unused")
public class DefaultPythonTaskRule extends RuleSource {

    public static final String CREATE_VIRTUAL_ENV_TASK = "createVirtualEnv";
    public static final String INSTALL_REQUIRED_DEPENDENCIES_TASK = "installRequiredDependencies";
    public static final String INSTALL_RUNTIME_DEPENDENCIES_TASK = "installRuntimeDependencies";
    public static final String INSTALL_TEST_DEPENDENCIES_TASK = "installTestDependencies";
    public static final String INSTALL_EDITABLE_TASK = "installEditable";
    public static final String PROJECT_SETUP_TASK = "projectSetup";

    private static final Logger logger = Logging.getLogger(DefaultPythonTaskRule.class);

    @Mutate
    public void addDefaultTasksForEachWheelBinary(final ModelMap<WheelBinarySpec> binarySpecs,
                                                  final PythonToolChainRegistry pythonToolChainRegistry,
                                                  final PythonPluginConfigurations configurations) {
        for (final WheelBinarySpec binarySpec : binarySpecs) {
            logger.info("Adding default tasks to {}", binarySpec.getName());
            binarySpec.tasks(new DefaultBinaryTaskCreateAction("",
                    pythonToolChainRegistry,
                    binarySpec.getPythonBuildDir(),
                    binarySpec.getVirtualEnvDir(),
                    binarySpec.getName(),
                    binarySpec.getTargetPlatform(),
                    configurations));
        }
    }

    @Mutate
    public void addDefaultTasksForEachSourceBinary(final ModelMap<SourceDistBinarySpec> binarySpecs,
                                                   final PythonToolChainRegistry pythonToolChainRegistry,
                                                   final PythonPluginConfigurations configurations) {
        for (final SourceDistBinarySpec binarySpec : binarySpecs) {
            logger.info("Adding default tasks to {}", binarySpec.getName());
            for (PythonTargetPlatform pythonPlatform : binarySpec.getTestPlatforms()) {
                String versionString = pythonPlatform.getVersion().getVersionString();

                File pythonBuildDir = new File(binarySpec.getBuildDir(), String.format("python-%s-%s", binarySpec.getName(), versionString));

                File venv = new File(pythonBuildDir, "venv");

                binarySpec.tasks(new DefaultBinaryTaskCreateAction(versionString,
                        pythonToolChainRegistry,
                        pythonBuildDir,
                        venv,
                        binarySpec.getName(),
                        binarySpec.getTestPlatforms().get(0),
                        configurations));
            }
        }
    }

    private class DefaultBinaryTaskCreateAction implements Action<BinaryTasksCollection> {

        private final String taskPostfix;
        private final PythonToolChainRegistry pythonToolChainRegistry;
        private final File pythonBuildDir;
        private final File virtualEnvDir;
        private final String binaryName;
        private final PythonTargetPlatform targetPlatform;
        private final PythonPluginConfigurations configurations;

        public DefaultBinaryTaskCreateAction(final String taskPostfix,
                                             final PythonToolChainRegistry pythonToolChainRegistry,
                                             final File pythonBuildDir,
                                             final File virtualEnvDir,
                                             final String binaryName,
                                             final PythonTargetPlatform targetPlatform,
                                             final PythonPluginConfigurations configurations) {
            this.taskPostfix = taskPostfix;
            this.pythonToolChainRegistry = pythonToolChainRegistry;
            this.pythonBuildDir = pythonBuildDir;
            this.virtualEnvDir = virtualEnvDir;
            this.binaryName = binaryName;
            this.targetPlatform = targetPlatform;
            this.configurations = configurations;
        }

        @Override
        public void execute(BinaryTasksCollection tasks) {
            final PythonToolChain toolChain = pythonToolChainRegistry.getForPlatform(targetPlatform);

            String createVirtualEnvTask = CREATE_VIRTUAL_ENV_TASK + taskPostfix;
            tasks.create(createVirtualEnvTask, VirtualEnvironmentBuild.class,
                    new CreateVirtualEnvConfigureAction(pythonBuildDir, virtualEnvDir, toolChain, configurations.getBootstrap(), binaryName));

            final String installRequiredDependencies = INSTALL_REQUIRED_DEPENDENCIES_TASK + taskPostfix;
            tasks.create(installRequiredDependencies, InstallDependenciesTask.class,
                    new DependencyConfigurationAction(pythonBuildDir, virtualEnvDir, toolChain, configurations.getVirtualEnv(), createVirtualEnvTask));

            final String installRuntimeDependencies = INSTALL_RUNTIME_DEPENDENCIES_TASK + taskPostfix;
            tasks.create(installRuntimeDependencies, InstallDependenciesTask.class,
                    new DependencyConfigurationAction(pythonBuildDir, virtualEnvDir, toolChain, configurations.getPython(), createVirtualEnvTask, installRequiredDependencies));

            final String installTestDependencies = INSTALL_TEST_DEPENDENCIES_TASK + taskPostfix;
            tasks.create(installTestDependencies, InstallDependenciesTask.class,
                    new DependencyConfigurationAction(pythonBuildDir, virtualEnvDir, toolChain, configurations.getPyTest(), createVirtualEnvTask, installRuntimeDependencies));

            final String installEditable = INSTALL_EDITABLE_TASK + taskPostfix;
            tasks.create(installEditable, InstallLocalProjectTask.class,
                    new InstallLocalConfigurationAction(pythonBuildDir, virtualEnvDir, toolChain, installTestDependencies));

            tasks.create(PROJECT_SETUP_TASK + taskPostfix, DefaultTask.class, new Action<Task>() {
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
