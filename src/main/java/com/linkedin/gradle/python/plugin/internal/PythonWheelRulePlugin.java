package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
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
import org.gradle.platform.base.internal.BinaryNamingSchemeBuilder;
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
    public void createBinaries(ModelMap<WheelBinarySpec> binaries, final PlatformResolvers platformResolver,
                               BinaryNamingSchemeBuilder namingSchemeBuilder, final WheelComponentSpec pythonComponent,
                               final BuildDirHolder buildDirHolder, final PythonToolChainRegistry pythonToolChainRegistry) {

        List<PythonPlatform> pythonPlatforms = SharedPythonInfrastructure.resolvePlatforms(platformResolver, pythonComponent);
        for (final PythonPlatform pythonPlatform : pythonPlatforms) {
            final String binaryName = buildBinaryName(pythonComponent, pythonPlatforms, pythonPlatform, namingSchemeBuilder);
            binaries.create(binaryName,
                    new PythonBinarySpecAction<WheelBinarySpec, WheelComponentSpec>(
                            pythonPlatform, pythonToolChainRegistry, buildDirHolder, binaryName, pythonComponent));
        }
    }

    private String buildBinaryName(WheelComponentSpec pythonComponent, List<PythonPlatform> selectedPlatforms,
                                   PythonPlatform platform, BinaryNamingSchemeBuilder namingSchemeBuilder) {
        BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder.withComponentName(pythonComponent.getName());

        if (selectedPlatforms.size() > 1) {
            componentBuilder = componentBuilder.withVariantDimension(platform.getName());
        }

        return componentBuilder.build().getLifecycleTaskName();
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks, final WheelBinarySpec binary,
                            final PythonPluginConfigurations configurations,
                            final PlatformResolvers platformResolver) {
        final PythonVersion version = binary.getTargetPlatform().getVersion();

        final String installEditable = SharedPythonInfrastructure.installPythonEnv(
                tasks, binary, version, configurations, binary.getComponentSpec().getName());

        String postFix = GUtil.toCamelCase(binary.getName());
        String createWheel = SharedPythonInfrastructure.taskNameGenerator(binary, version, "buildWheel");
        tasks.create(createWheel, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn(installEditable);
            }
        });
    }
}
