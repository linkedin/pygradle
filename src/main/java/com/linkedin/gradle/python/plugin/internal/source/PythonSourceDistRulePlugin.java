package com.linkedin.gradle.python.plugin.internal.source;

import com.linkedin.gradle.python.spec.binary.SourceDistBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.DefaultSourceDistBinarySpec;
import com.linkedin.gradle.python.spec.component.SourceDistComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.DefaultSourceDistComponentSpec;
import com.linkedin.gradle.python.spec.component.internal.PythonTargetPlatform;
import java.util.List;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.BinaryType;
import org.gradle.platform.base.BinaryTypeBuilder;
import org.gradle.platform.base.ComponentBinaries;
import org.gradle.platform.base.ComponentType;
import org.gradle.platform.base.ComponentTypeBuilder;
import org.gradle.platform.base.internal.PlatformResolvers;


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

//    @Validate
//    public void validateThatThereIsAtLeastOnePythonVersion(final SourceDistComponentSpec pythonComponent){
//        if(pythonComponent.getTestPlatforms().isEmpty()) {
//            throw new GradleException("At least one python version must be defined");
//        }
//    }

    @ComponentBinaries
    public void createBinaries(final ModelMap<SourceDistBinarySpec> binaries,
                               final PlatformResolvers platformResolver,
                               final SourceDistComponentSpec pythonComponent) {

        List<PythonTargetPlatform> pythonPlatforms = pythonComponent.getTargetPlatforms();
        binaries.create(pythonComponent.getName(), new PythonSourceDistSpecAction(pythonPlatforms));
    }

//    @BinaryTasks
//    public void createTasks(final ModelMap<Task> tasks,
//                            final SourceDistBinarySpec binary,
//                            final BuildDirHolder buildDirHolder,
//                            final PythonPluginConfigurations configurations,
//                            final PythonToolChainRegistry pythonToolChainRegistry) {
//        PythonTargetPlatform pythonPlatform = binary.getTestPlatforms().get(0);
//        SharedPythonInfrastructure sharedPythonInfrastructure = new SharedPythonInfrastructure(pythonPlatform, binary, buildDirHolder);
//
//        PythonToolChain toolChain = pythonToolChainRegistry.getForPlatform(pythonPlatform);
//
//        tasks.create("buildSourceDist", BuildSourceDistTask.class,
//                     new BasePythonTaskAction<BuildSourceDistTask>(sharedPythonInfrastructure.getPythonBuildDir(),
//                                                                   sharedPythonInfrastructure.getVirtualEnvDir(),
//                                                                   toolChain) {
//                         @Override
//                         public void configure(BuildSourceDistTask task) {
//                             task.dependsOn(createTaskNames(binary));
//                             for (PythonSourceSet pythonSourceSet : binary.getSources()
//                                 .withType(PythonSourceSet.class)
//                                 .values()) {
//                                 task.sourceSet(pythonSourceSet.getSource());
//                             }
//                         }
//                     });
//    }
//
//    private List<String> createTaskNames(final SourceDistBinarySpec binary) {
//        ArrayList<String> strings = new ArrayList<String>();
//        for (PythonPlatform pythonPlatform : binary.getTestPlatforms()) {
//            strings.add(DefaultPythonTaskRule.PROJECT_SETUP_TASK + pythonPlatform.getVersion().getVersionString());
//        }
//
//        return strings;
//    }
}
