package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultSourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.ResolvedPythonEnvironment;
import com.linkedin.gradle.python.spec.component.SourceDistComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultSourceDistComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import com.linkedin.gradle.python.tasks.BuildSourceDistTask;
import com.linkedin.gradle.python.tasks.internal.configuration.BuildSourceDistConfigurationAction;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.ModelMap;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.util.GUtil;

import java.io.File;
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
    public void createBinaries(final ModelMap<SourceDistBinarySpec> binaries,
                               final PlatformResolvers platformResolver,
                               final SourceDistComponentSpec pythonComponent,
                               @Path("buildDir") final File buildDir) {

        List<PythonTargetPlatform> pythonPlatforms = pythonComponent.getTargetPlatforms();
        binaries.create(pythonComponent.getName(), new PythonSourceDistSpecAction(pythonPlatforms, buildDir));
    }

    @BinaryTasks
    public void createTasks(final ModelMap<Task> tasks,
                            final SourceDistBinarySpec binary,
                            final BuildDirHolder buildDirHolder,
                            final PythonPluginConfigurations configurations,
                            final PythonToolChainRegistry pythonToolChainRegistry) {
        String postFix = GUtil.toCamelCase(binary.getName());

        ResolvedPythonEnvironment resolvedPythonEnvironment = binary.getPythonEnvironments().get(0);
        BuildSourceDistConfigurationAction configAction = new BuildSourceDistConfigurationAction(
                resolvedPythonEnvironment.getBuildDir(),
                resolvedPythonEnvironment.getVenvDir(),
                pythonToolChainRegistry.getForPlatform(resolvedPythonEnvironment.getTargetPlatform()));

        tasks.create("build" + postFix, BuildSourceDistTask.class, configAction);
    }
}
