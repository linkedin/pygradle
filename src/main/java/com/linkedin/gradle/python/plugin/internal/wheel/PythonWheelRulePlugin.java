package com.linkedin.gradle.python.plugin.internal.wheel;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.SharedPythonInfrastructure;
import com.linkedin.gradle.python.spec.binary.WheelBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.component.WheelComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultWheelComponentSpec;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.util.GUtil;

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
    public void createBinaries(ModelMap<WheelBinarySpec> binaries,
                               final PlatformResolvers platformResolver,
                               final WheelComponentSpec pythonComponent,
                               final PythonToolChainRegistry pythonToolChainRegistry) {

        List<PythonPlatform> pythonPlatforms = SharedPythonInfrastructure.resolvePlatforms(platformResolver, pythonComponent);
        for (final PythonPlatform pythonPlatform : pythonPlatforms) {
            binaries.create("wheel",new PythonWheelSpecAction(pythonPlatform));
        }
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks,
                            final WheelBinarySpec binary,
                            final BuildDirHolder buildDirHolder,
                            final PythonPluginConfigurations configurations,
                            final PythonToolChainRegistry pythonToolChainRegistry) {
        final PythonVersion version = binary.getTargetPlatform().getVersion();

        SharedPythonInfrastructure infra = new SharedPythonInfrastructure(binary.getTargetPlatform(), binary, buildDirHolder);
        final String installEditable = infra.installPythonEnv(tasks, configurations, pythonToolChainRegistry);

        String postFix = GUtil.toCamelCase(binary.getName());
        String createWheel = SharedPythonInfrastructure.taskNameGenerator(version, "buildWheel");
        tasks.create(createWheel, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn(installEditable);
            }
        });
    }
}
