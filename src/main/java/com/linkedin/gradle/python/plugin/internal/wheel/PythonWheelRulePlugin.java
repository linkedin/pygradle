package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultWheelComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.AddDependsOnTaskAction;
import com.linkedin.gradle.python.tasks.internal.configuration.PyTestConfigurationAction;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.List;


@SuppressWarnings("unused")
public class PythonWheelRulePlugin extends RuleSource {
    public static final Logger log = Logging.getLogger(PythonWheelRulePlugin.class);

    @ComponentType
    public void register(ComponentTypeBuilder<WheelComponentSpec> builder) {
        builder.defaultImplementation(DefaultWheelComponentSpec.class);
    }

    @BinaryType
    public void registerWheel(BinaryTypeBuilder<WheelBinarySpec> builder) {
        builder.defaultImplementation(DefaultWheelBinarySpec.class);
    }

    @ComponentBinaries
    public void createBinaries(final ModelMap<WheelBinarySpec> binaries,
                               final PlatformResolvers platformResolver,
                               final WheelComponentSpec pythonComponent,
                               final PythonToolChainRegistry pythonToolChainRegistry,
                               @Path("buildDir") final File buildDir) {
        List<PythonTargetPlatform> pythonPlatforms = pythonComponent.getTargetPlatforms();
        for (final PythonTargetPlatform pythonPlatform : pythonPlatforms) {
            binaries.create(pythonComponent.getName() + pythonPlatform.getVersion().getVersionString(), new PythonWheelSpecAction(pythonPlatform, buildDir));
        }
    }

    @BinaryTasks
    public void createTasks(final ModelMap<Task> tasks,
                            final WheelBinarySpec binary,
                            final BuildDirHolder buildDirHolder,
                            final PythonPluginConfigurations configurations,
                            final PythonToolChainRegistry pythonToolChainRegistry) {
        final PythonVersion version = binary.getTargetPlatform().getVersion();

        String postFix = GUtil.toCamelCase(binary.getName());
        tasks.create("build" + postFix, BuildWheelTask.class, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn(binary.getProjectSetupTask());
            }
        });
    }

//    @Mutate
//    public void createTestTasks(final ModelMap<Task> tasks,
//                                final ModelMap<WheelBinarySpec> wheelBinarySpecs,
//                                final PythonToolChainRegistry pythonToolChainRegistry) {
//
//        for (WheelBinarySpec wheelBinarySpec : wheelBinarySpecs) {
//            PythonToolChain toolChain = pythonToolChainRegistry.getForPlatform(wheelBinarySpec.getTargetPlatform());
//            PyTestConfigurationAction configAction = new PyTestConfigurationAction(wheelBinarySpec.getBuildDir(), wheelBinarySpec.getVirtualEnvDir(), toolChain);
//            String testTaskName = "test" + wheelBinarySpec.getName();
//            tasks.create(testTaskName, PythonTestTask.class, configAction);
//            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, new AddDependsOnTaskAction(testTaskName));
//        }
//    }
}
