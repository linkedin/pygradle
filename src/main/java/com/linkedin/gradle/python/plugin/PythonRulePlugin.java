package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.PythonPlatformResolver;
import com.linkedin.gradle.python.internal.platform.*;
import com.linkedin.gradle.python.spec.WheelBinarySpec;
import com.linkedin.gradle.python.spec.WheelComponentSpec;
import com.linkedin.gradle.python.spec.internal.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.internal.DefaultWheelComponentSpec;
import com.linkedin.gradle.python.spec.internal.PythonBinarySpec;
import com.linkedin.gradle.python.spec.internal.PythonComponentSpec;
import com.linkedin.gradle.python.tasks.*;
import com.linkedin.gradle.python.tasks.internal.BasePythonTaskAction;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
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
    public static final String GENERATE_SETUP_PY = "generateSetupPy";

    @LanguageType
    public void registerLanguage(LanguageTypeBuilder<PythonSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonSourceSet.class);
    }

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

    private String buildBinaryName(WheelComponentSpec pythonComponent, List<PythonPlatform> selectedPlatforms,
                                   PythonPlatform platform, BinaryNamingSchemeBuilder namingSchemeBuilder) {
        BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder.withComponentName(pythonComponent.getName());

        if (selectedPlatforms.size() > 1) {
            componentBuilder = componentBuilder.withVariantDimension(platform.getName());
        }

        return componentBuilder.build().getLifecycleTaskName();
    }

    private List<PythonPlatform> resolvePlatforms(final PlatformResolvers platformResolver, PythonComponentSpec wheelComponentSpec) {
        List<PlatformRequirement> targetPlatforms = wheelComponentSpec.getTargetPlatforms();
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
        final String createVirtualEnv = taskNameGenerator(binary, version, "createVirtualEnv");
        final WheelComponentSpec componentSpec = binary.getComponentSpec();

        tasks.create(GENERATE_SETUP_PY, SetupPyTask.class, new Action<SetupPyTask>() {
            @Override
            public void execute(SetupPyTask setupPyTask) {
                setupPyTask.setComponentSpec(componentSpec);
                setupPyTask.setSourceDir((PythonSourceSet) componentSpec.getSources().get("python"));
                setupPyTask.setPythonPlatforms(resolvePlatforms(platformResolver, componentSpec));
            }
        });

        tasks.create(createVirtualEnv, VirtualEnvironmentBuild.class, new BasePythonTaskAction<VirtualEnvironmentBuild>(binary) {
            @Override
            public void configure(VirtualEnvironmentBuild task) {
                task.setVirtualEnvFiles(configurations.getBootstrap().getConfiguration());
                task.setActivateScriptName(String.format("activate-%s-%s", componentSpec.getName(), version.getVersionString()));
            }
        });

        final String installDependencies = taskNameGenerator(binary, version, "installRequiredDependencies");
        tasks.create(installDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(createVirtualEnv);
                task.setVirtualEnvFiles(configurations.getVirtualEnv().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "requiredDependencies"));
            }
        });

        final String installRuntimeDependencies = taskNameGenerator(binary, version, "installRuntimeDependencies");
        tasks.create(installRuntimeDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(installDependencies);
                task.setVirtualEnvFiles(configurations.getPython().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "dependencies"));
            }
        });

        final String installTestDependencies = taskNameGenerator(binary, version, "installTestDependencies");
        tasks.create(installTestDependencies, InstallDependenciesTask.class, new BasePythonTaskAction<InstallDependenciesTask>(binary) {
            @Override
            public void configure(InstallDependenciesTask task) {
                task.dependsOn(installRuntimeDependencies);
                task.setVirtualEnvFiles(configurations.getPyTest().getConfiguration());
                task.setInstallDir(new File(binary.getVirtualEnvDir(), "testDependencies"));
            }
        });

        final String installEditable = taskNameGenerator(binary, version, "installEditable");
        tasks.create(installEditable, InstallLocalProject.class, new BasePythonTaskAction<InstallLocalProject>(binary) {
            @Override
            public void configure(InstallLocalProject task) {
                task.dependsOn(GENERATE_SETUP_PY);
                task.dependsOn(installTestDependencies);
            }
        });

        String postFix = GUtil.toCamelCase(binary.getName());
        String createWheel = taskNameGenerator(binary, version, "buildWheel");
        tasks.create(createWheel, new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.dependsOn(installEditable);
            }
        });
    }

    private String taskNameGenerator(PythonBinarySpec binarySpec, PythonVersion version, String taskName) {
        String binaryName = GUtil.toLowerCamelCase(binarySpec.getName());
        String camelCaseTaskName = GUtil.toCamelCase(taskName);
        return String.format("%s%s%s", binaryName, camelCaseTaskName, version.getVersionString());
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
    void createPythonSourceSets(ModelMap<WheelComponentSpec> binaries) {
        binaries.all(new Action<WheelComponentSpec>() {
            @Override
            public void execute(WheelComponentSpec wheelComponentSpec) {
                wheelComponentSpec.getSources().create("python", PythonSourceSet.class, new Action<PythonSourceSet>() {
                    @Override
                    public void execute(PythonSourceSet defaultPythonSourceSet) {
                        defaultPythonSourceSet.getSource().srcDir("src/main/python");
                        defaultPythonSourceSet.getSource().include("**/*.py");
                    }
                });
                wheelComponentSpec.getSources().create("pythonTest", PythonSourceSet.class, new Action<PythonSourceSet>() {
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
