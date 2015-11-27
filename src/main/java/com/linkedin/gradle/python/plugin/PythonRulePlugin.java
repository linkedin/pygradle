package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.PythonPlatformResolver;
import com.linkedin.gradle.python.internal.platform.*;
import com.linkedin.gradle.python.spec.DefaultPythonComponentSpec;
import com.linkedin.gradle.python.spec.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.PythonComponentSpec;
import com.linkedin.gradle.python.spec.WheelBinarySpec;
import com.linkedin.gradle.python.tasks.InstallDependencies;
import com.linkedin.gradle.python.tasks.InstallLocalProject;
import com.linkedin.gradle.python.tasks.SetupPyTask;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.*;
import org.gradle.platform.base.*;
import org.gradle.platform.base.internal.*;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("unused")
public class PythonRulePlugin extends RuleSource {
    public static final Logger log = Logging.getLogger(PythonLangPlugin.class);

    @LanguageType
    public void registerLanguage(LanguageTypeBuilder<PythonSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonSourceSet.class);
    }

    @ComponentType
    public void register(ComponentTypeBuilder<PythonComponentSpec> builder) {
        builder.defaultImplementation(DefaultPythonComponentSpec.class);
    }

    @BinaryType
    public void registerJar(BinaryTypeBuilder<WheelBinarySpec> builder) {
        builder.defaultImplementation(DefaultWheelBinarySpec.class);
    }

    @ComponentBinaries
    public void createBinaries(ModelMap<WheelBinarySpec> binaries, final PlatformResolvers platformResolver,
                               BinaryNamingSchemeBuilder namingSchemeBuilder, final PythonComponentSpec pythonComponent,
                               final BuildDirHolder buildDirHolder, final PythonToolChainRegistry pythonToolChainRegistry) {

        List<PythonPlatform> pythonPlatforms = resolvePlatforms(platformResolver, pythonComponent);
        for (final PythonPlatform pythonPlatform : pythonPlatforms) {
            String binaryName = buildBinaryName(pythonComponent, pythonPlatforms, pythonPlatform, namingSchemeBuilder);
            binaries.create(binaryName, new Action<WheelBinarySpec>() {
                @Override
                public void execute(WheelBinarySpec wheelBinarySpec) {
                    wheelBinarySpec.setTargetPlatform(pythonPlatform);
                    wheelBinarySpec.setToolChain(pythonToolChainRegistry.getForPlatform(pythonPlatform));

                    PythonPlatform platform = wheelBinarySpec.getTargetPlatform();
                    final File pythonBuildDir = new File(buildDirHolder.getDir(), "python-" + platform.getVersion().getVersionString());
                    final File virtualEnvDir = new File(pythonBuildDir, "venv");

                    wheelBinarySpec.setVirtualEnvDir(virtualEnvDir);
                    wheelBinarySpec.setPythonBuildDir(pythonBuildDir);
                    wheelBinarySpec.setComponentSpec(pythonComponent);
                }
            });
        }
    }

    private String buildBinaryName(PythonComponentSpec pythonComponent, List<PythonPlatform> selectedPlatforms,
                                   PythonPlatform platform, BinaryNamingSchemeBuilder namingSchemeBuilder) {
        BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder.withComponentName(pythonComponent.getName());

        if (selectedPlatforms.size() > 1) {
            componentBuilder = componentBuilder.withVariantDimension(platform.getName());
        }

        return componentBuilder.build().getLifecycleTaskName();
    }

    private List<PythonPlatform> resolvePlatforms(final PlatformResolvers platformResolver, PythonComponentSpec pythonComponentSpec) {
        List<PlatformRequirement> targetPlatforms = pythonComponentSpec.getTargetPlatforms();
        if (targetPlatforms.isEmpty()) {
            targetPlatforms = Collections.singletonList(
                    DefaultPlatformRequirement.create(DefaultPythonPlatform.current().getName()));
        }
        return CollectionUtils.collect(targetPlatforms, new Transformer<PythonPlatform, PlatformRequirement>() {
            @Override
            public PythonPlatform transform(PlatformRequirement platformRequirement) {
                return platformResolver.resolve(PythonPlatform.class, platformRequirement);
            }
        });
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks, final WheelBinarySpec binary,
                            final PythonPluginConfigurations configurations,
                            final PlatformResolvers platformResolver) {
        final PythonVersion version = binary.getTargetPlatform().getVersion();
        final String createVirtualEnv = "createVirtualEnv" + version.getVersionString();
        final String generateSetupPy = "generateSetupPy";
        final PythonComponentSpec componentSpec = binary.getComponentSpec();

        if (tasks.get(generateSetupPy) == null) {
            tasks.create(generateSetupPy, SetupPyTask.class, new Action<SetupPyTask>() {
                @Override
                public void execute(SetupPyTask setupPyTask) {
                    setupPyTask.setComponentSpec(componentSpec);
                    setupPyTask.setSourceDir((PythonSourceSet) componentSpec.getSources().get("python"));
                    setupPyTask.setPythonPlatforms(resolvePlatforms(platformResolver, componentSpec));
                }
            });
        }

        tasks.create(createVirtualEnv, VirtualEnvironmentBuild.class, new Action<VirtualEnvironmentBuild>() {
            @Override
            public void execute(VirtualEnvironmentBuild task) {
                task.setPythonToolChain(binary.getToolChain());
                task.setVenvDir(binary.getVirtualEnvDir());
                task.setPythonBuilDir(binary.getPythonBuildDir());
                task.setVirtualEnvFiles(configurations.getBootstrap().getConfiguration());
            }
        });

        final String installDependencies = "installRequiredDependencies" + version.getVersionString();
        tasks.create(installDependencies, InstallDependencies.class, new Action<InstallDependencies>() {
            @Override
            public void execute(InstallDependencies task) {
                task.dependsOn(createVirtualEnv);
                task.setPythonToolChain(binary.getToolChain());
                task.setPythonBuilDir(binary.getPythonBuildDir());
                task.setVenvDir(binary.getVirtualEnvDir());
                task.setVirtualEnvFiles(configurations.getVirtualEnv().getConfiguration());
            }
        });

        final String installRuntimeDependencies = "installRuntimeDependencies" + version.getVersionString();
        tasks.create(installRuntimeDependencies, InstallDependencies.class, new Action<InstallDependencies>() {
            @Override
            public void execute(InstallDependencies task) {
                task.dependsOn(installDependencies);
                task.setPythonToolChain(binary.getToolChain());
                task.setPythonBuilDir(binary.getPythonBuildDir());
                task.setVenvDir(binary.getVirtualEnvDir());
                task.setVirtualEnvFiles(configurations.getPython().getConfiguration());
            }
        });

        final String installTestDependencies = "installTestDependencies" + version.getVersionString();
        tasks.create(installTestDependencies, InstallDependencies.class, new Action<InstallDependencies>() {
            @Override
            public void execute(InstallDependencies task) {
                task.dependsOn(installRuntimeDependencies);
                task.setPythonToolChain(binary.getToolChain());
                task.setPythonBuilDir(binary.getPythonBuildDir());
                task.setVenvDir(binary.getVirtualEnvDir());
                task.setVirtualEnvFiles(configurations.getPyTest().getConfiguration());
            }
        });

        final String installEditable = "installEditable" + version.getVersionString();
        tasks.create(installEditable, InstallLocalProject.class, new Action<InstallLocalProject>() {
            @Override
            public void execute(InstallLocalProject installLocalProject) {
                installLocalProject.dependsOn(generateSetupPy);
                installLocalProject.dependsOn(installTestDependencies);
            }
        });

        String postFix = GUtil.toCamelCase(binary.getName());
        String createWheel = "create" + postFix + "Wheel" + version.getVersionString();
        tasks.create(createWheel, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn(installEditable);
            }
        });
    }

    @Mutate
    public void registerPlatformResolver(PlatformResolvers platformResolvers) {
        platformResolvers.register(new PythonPlatformResolver());
    }

    @Model
    public BinaryNamingSchemeBuilder binaryNamingSchemeBuilder() {
        return new DefaultBinaryNamingSchemeBuilder();
    }

    @Model
    public PythonToolChainRegistry pythonToolChain(ServiceRegistry serviceRegistry, PlatformResolvers platformResolvers) {
        ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
        return new DefaultPythonToolChainRegistry(execActionFactory);
    }

    @Model
    public BuildDirHolder buildDirHolder(@Path("buildDir") File buildDir) {
        return new BuildDirHolder(buildDir);
    }

    @Model
    PythonPluginConfigurations configurations(ExtensionContainer extensions) {
        return extensions.getByType(PythonPluginConfigurations.class);
    }

    @Mutate
    void createPythonSourceSets(ModelMap<PythonComponentSpec> binaries, final ServiceRegistry serviceRegistry) {
        final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
        final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
        binaries.all(new Action<PythonComponentSpec>() {
            @Override
            public void execute(PythonComponentSpec pythonComponentSpec) {
                pythonComponentSpec.getSources().create("python", PythonSourceSet.class, new Action<PythonSourceSet>() {
                    @Override
                    public void execute(PythonSourceSet defaultPythonSourceSet) {
                        defaultPythonSourceSet.getSource().srcDir("src/main/python");
                        defaultPythonSourceSet.getSource().include("**/*.py");
                    }
                });
                pythonComponentSpec.getSources().create("pythonTest", PythonSourceSet.class, new Action<PythonSourceSet>() {
                    @Override
                    public void execute(PythonSourceSet defaultPythonSourceSet) {
                        defaultPythonSourceSet.getSource().srcDir("src/test/python");
                        defaultPythonSourceSet.getSource().include("**/*.py");
                    }
                });
            }
        });
    }
}
