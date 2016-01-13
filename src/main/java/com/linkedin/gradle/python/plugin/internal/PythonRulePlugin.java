package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultSourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultPythonComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpecInternal;
import com.linkedin.gradle.python.spec.component.internal.PythonEnvironment;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.InstallDependenciesTask;
import com.linkedin.gradle.python.tasks.InstallLocalProjectTask;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import com.linkedin.gradle.python.tasks.internal.AddDependsOnTaskAction;
import com.linkedin.gradle.python.tasks.internal.configuration.DistConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.CreateVirtualEnvConfigureAction;
import com.linkedin.gradle.python.tasks.internal.configuration.DependencyConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.InstallLocalConfigurationAction;
import com.linkedin.gradle.python.tasks.internal.configuration.PyTestConfigurationAction;
import java.io.File;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.BinaryType;
import org.gradle.platform.base.BinaryTypeBuilder;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.GUtil;


public class PythonRulePlugin extends RuleSource {

  public static final String CREATE_VIRTUAL_ENV_TASK = "createVirtualEnv";
  public static final String INSTALL_REQUIRED_DEPENDENCIES_TASK = "installRequiredDependencies";
  public static final String INSTALL_RUNTIME_DEPENDENCIES_TASK = "installRuntimeDependencies";
  public static final String INSTALL_TEST_DEPENDENCIES_TASK = "installTestDependencies";
  public static final String INSTALL_EDITABLE_TASK = "installEditable";

  private static final Logger logger = Logging.getLogger(PythonRulePlugin.class);

  @ComponentType
  public void register(ComponentTypeBuilder<PythonComponentSpec> builder) {
    builder.defaultImplementation(DefaultPythonComponentSpec.class);
    builder.internalView(PythonComponentSpecInternal.class);
  }

  @BinaryType
  public void registerSourceDist(BinaryTypeBuilder<SourceDistBinarySpec> builder) {
    builder.defaultImplementation(DefaultSourceDistBinarySpec.class);
  }

  @BinaryType
  public void registerWheel(BinaryTypeBuilder<WheelBinarySpec> builder) {
    builder.defaultImplementation(DefaultWheelBinarySpec.class);
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

  @Validate
  void validateComponent(ModelMap<PythonComponentSpecInternal> componentSpecMap) {
    for (PythonComponentSpecInternal pythonComponentSpecInternal : componentSpecMap) {
      if (pythonComponentSpecInternal.getPythonEnvironments().size() == 0) {
        throw new GradleException(pythonComponentSpecInternal.getName() + " must have at least 1 targetPlatform");
      }
    }
  }

  @Mutate
  void createVirtalEnvironments(ModelMap<Task> taskContainer, final ModelMap<PythonComponentSpecInternal> specs,
      final PythonPluginConfigurations configurations) {
    logger.debug("Creating Virtual Envs");
    for (PythonComponentSpecInternal spec : specs) {
      for (PythonEnvironment pythonEnvironment : spec.getPythonEnvironments()) {

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
      for (PythonEnvironment pythonEnvironment : spec.getPythonEnvironments()) {

        PyTestConfigurationAction configAction = new PyTestConfigurationAction(pythonEnvironment, spec.getSources());

        String taskName = "pyTest" + pythonEnvironment.getVersion().getVersionString();
        tasks.create(taskName, PythonTestTask.class, configAction);
        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, new AddDependsOnTaskAction(taskName));
      }
    }
  }

  @ComponentBinaries
  public void createSourceDistBinaries(ModelMap<PythonBinarySpec> binarySpecs, final PythonComponentSpecInternal spec) {
    final List<PythonEnvironment> pythonEnvironments = spec.getPythonEnvironments();
    if (spec.getSourceDist()) {
      binarySpecs.create(spec.getName() + "SourceDist", SourceDistBinarySpec.class,
          new SourceDistAction(pythonEnvironments.get(0)));
    }

    if (spec.getWheels()) {
      for (PythonEnvironment pythonEnvironment : pythonEnvironments) {
        String name = spec.getName() + "Wheel" + pythonEnvironment.getVersion().getVersionString();
        binarySpecs.create(name, WheelBinarySpec.class, new WheelAction(pythonEnvironment));
      }
    }
  }

  @BinaryTasks
  public void createWheelTask(ModelMap<Task> tasks, final WheelBinarySpec spec) {
    String postFix = GUtil.toCamelCase(spec.getName());
    tasks.create("create" + postFix, BuildWheelTask.class, new DistConfigurationAction(spec.getPythonEnvironment(), spec.getSources()));
  }

  @BinaryTasks
  public void createSourceDistTask(ModelMap<Task> tasks, final SourceDistBinarySpec spec) {
    String postFix = GUtil.toCamelCase(spec.getName());
    tasks.create("create" + postFix, BuildSourceDistTask.class, new DistConfigurationAction(spec.getPythonEnvironment(), spec.getSources()));
  }
}
