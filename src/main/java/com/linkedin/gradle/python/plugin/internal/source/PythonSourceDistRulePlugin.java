package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.internal.toolchain.PythonToolChain;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.plugin.internal.SharedPythonInfrastructure;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultSourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.SourceDistComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultSourceDistComponentSpec;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.PlatformResolvers;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class PythonSourceDistRulePlugin extends RuleSource {
    public static final Logger log = Logging.getLogger(PythonSourceDistRulePlugin.class);

    @ComponentType
    public void register(ComponentTypeBuilder<SourceDistComponentSpec> builder) {
        builder.defaultImplementation(DefaultSourceDistComponentSpec.class);
    }

    @BinaryType
    public void registerSourceDist(BinaryTypeBuilder<SourceDistBinarySpec> builder) {
        builder.defaultImplementation(DefaultSourceDistBinarySpec.class);
    }

    @ComponentBinaries
    public void createBinaries(ModelMap<SourceDistBinarySpec> binaries,
                               final PlatformResolvers platformResolver,
                               final SourceDistComponentSpec pythonComponent) {

        List<PythonPlatform> pythonPlatforms = SharedPythonInfrastructure.resolvePlatforms(platformResolver, pythonComponent);
        binaries.create(pythonComponent.getName(), new PythonSourceDistSpecAction(pythonPlatforms));
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks,
                            final SourceDistBinarySpec binary,
                            final BuildDirHolder buildDirHolder,
                            final PythonPluginConfigurations configurations,
                            final PythonToolChainRegistry pythonToolChainRegistry) {
        final List<String> taskNames = new ArrayList<String>();

        for (PythonPlatform pythonPlatform : binary.getTargetPlatforms()) {
            SharedPythonInfrastructure infra = new SharedPythonInfrastructure(pythonPlatform, binary, buildDirHolder);
            PythonVersion version = pythonPlatform.getVersion();
            String installEditable = infra.installPythonEnv(tasks,
                    configurations,
                    pythonToolChainRegistry);

            taskNames.add(installEditable);
        }

        createSourceDist(tasks, binary, buildDirHolder, pythonToolChainRegistry, taskNames);
    }

    private void createSourceDist(ModelMap<Task> tasks,
                                  final SourceDistBinarySpec binary,
                                  final BuildDirHolder buildDirHolder,
                                  final PythonToolChainRegistry pythonToolChainRegistry,
                                  final List<String> taskNames) {
        PythonPlatform pythonPlatform = binary.getTargetPlatforms().get(0);
        SharedPythonInfrastructure sharedPythonInfrastructure = new SharedPythonInfrastructure(
                pythonPlatform,
                binary,
                buildDirHolder);

        PythonToolChain toolChain = pythonToolChainRegistry.getForPlatform(pythonPlatform);

        tasks.create("buildSourceDist", BuildSourceDistTask.class,
                new BasePythonTaskAction<BuildSourceDistTask>(sharedPythonInfrastructure.getPythonBuildDir(),
                        sharedPythonInfrastructure.getVirtualEnvDir(),
                        toolChain) {
                    @Override
                    public void configure(BuildSourceDistTask task) {
                        task.dependsOn(taskNames);
                        for (PythonSourceSet pythonSourceSet : binary.getSources().withType(PythonSourceSet.class).values()) {
                            task.sourceSet(pythonSourceSet.getSource());
                        }

                    }
                });
    }
}
