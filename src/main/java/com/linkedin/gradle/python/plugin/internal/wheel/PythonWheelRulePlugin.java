package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.SharedPythonInfrastructure;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultWheelComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import com.linkedin.gradle.python.tasks.BuildWheelTask;
import com.linkedin.gradle.python.tasks.PythonTestTask;
import com.linkedin.gradle.python.tasks.internal.AddDependsOnTaskAction;
import com.linkedin.gradle.python.tasks.internal.PyTestAction;
import java.io.File;
import java.util.List;
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
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.BinaryType;
import org.gradle.platform.base.BinaryTypeBuilder;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.util.GUtil;


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
    public void createBinaries(final ModelMap<WheelBinarySpec> binaries, final PlatformResolvers platformResolver, final WheelComponentSpec pythonComponent,
                               final PythonToolChainRegistry pythonToolChainRegistry,
        @Path("buildDir") final File buildDir) {

        List<PythonTargetPlatform> pythonPlatforms = pythonComponent.getTargetPlatforms();
        for (final PythonTargetPlatform pythonPlatform : pythonPlatforms) {
            binaries.create(pythonComponent.getName() + pythonPlatform.getVersion().getVersionString(), new PythonWheelSpecAction(pythonPlatform, buildDir));
        }
    }

    @BinaryTasks
    public void createTasks(final ModelMap<Task> tasks, final WheelBinarySpec binary, final BuildDirHolder buildDirHolder, final PythonPluginConfigurations configurations,
                            final PythonToolChainRegistry pythonToolChainRegistry) {
        final PythonVersion version = binary.getTargetPlatform().getVersion();

        String postFix = GUtil.toCamelCase(binary.getName());
        tasks.create("buildWheel", BuildWheelTask.class, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn("projectSetup");
            }
        });
    }

//    @Mutate
//    public void createTestTasks(final ModelMap<Task> tasks, final ModelMap<WheelBinarySpec> wheelBinarySpecs, final BuildDirHolder buildDirHolder,
//                                final PythonToolChainRegistry pythonToolChainRegistry) {
//        for (WheelBinarySpec wheelBinarySpec : wheelBinarySpecs) {
//            SharedPythonInfrastructure sharedInfra = new SharedPythonInfrastructure(wheelBinarySpec.getTargetPlatform(), wheelBinarySpec, buildDirHolder);
//
//            PythonToolChain platform = pythonToolChainRegistry.getForPlatform(wheelBinarySpec.getTargetPlatform());
//            PyTestAction configAction = new PyTestAction(sharedInfra, platform);
//
//            String testTaskName = createTestTaskName(wheelBinarySpec);
//            tasks.create(testTaskName, PythonTestTask.class, configAction);
//            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, new AddDependsOnTaskAction(testTaskName));
//        }
//    }

    private String createTestTaskName(WheelBinarySpec wheelBinarySpec) {
        return SharedPythonInfrastructure.taskNameGenerator(wheelBinarySpec.getTargetPlatform().getVersion(), "testWheel");
    }
}
