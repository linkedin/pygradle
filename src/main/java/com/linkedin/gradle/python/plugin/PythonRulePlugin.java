package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.PythonPlatformResolver;
import com.linkedin.gradle.python.internal.platform.DefaultPythonPlatform;
import com.linkedin.gradle.python.internal.platform.DefaultPythonToolChain;
import com.linkedin.gradle.python.internal.platform.DefaultPythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonPlatform;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonVersion;
import com.linkedin.gradle.python.spec.DefaultPythonComponentSpec;
import com.linkedin.gradle.python.spec.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.PythonComponentSpec;
import com.linkedin.gradle.python.spec.WheelBinarySpec;
import com.linkedin.gradle.python.tasks.InstallDependencies;
import com.linkedin.gradle.python.tasks.VirtualEnvironmentBuild;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
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
import org.gradle.model.Model;
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
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;
import org.gradle.platform.base.internal.BinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.DefaultBinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.DefaultPlatformRequirement;
import org.gradle.platform.base.internal.PlatformRequirement;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.CollectionUtils;

import static org.apache.commons.lang.StringUtils.capitalize;


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
  public void createBinaries(ModelMap<WheelBinarySpec> binaries, final PlatformResolvers platformResolver, BinaryNamingSchemeBuilder namingSchemeBuilder,
      final PythonComponentSpec pythonComponent, final BuildDirHolder buildDirHolder, final PythonToolChainRegistry pythonToolChainRegistry) {

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
        }
      });
    }
  }

  private String buildBinaryName(PythonComponentSpec pythonComponent, List<PythonPlatform> selectedPlatforms, PythonPlatform platform, BinaryNamingSchemeBuilder namingSchemeBuilder) {
    BinaryNamingSchemeBuilder componentBuilder = namingSchemeBuilder.withComponentName(pythonComponent.getName());

    if(selectedPlatforms.size() > 1) {
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
  public void createTasks(ModelMap<Task> tasks, final WheelBinarySpec binary, final PythonPluginConfigurations configurations) {
    final PythonVersion version = binary.getTargetPlatform().getVersion();
    final String createVirtualEnv = "createVirtualEnv" + version.getVersionString();

    tasks.create(createVirtualEnv, VirtualEnvironmentBuild.class, new Action<VirtualEnvironmentBuild>() {
      @Override
      public void execute(VirtualEnvironmentBuild task) {
        task.setPythonToolChain(binary.getToolChain());
        task.setVenvDir(binary.getVirtualEnvDir());
        task.setPythonBuilDir(binary.getPythonBuildDir());
        task.setVirtualEnvFiles(configurations.getBootstrap().getConfiguration());
      }
    });

    final String installDependencies = "installDependencies" + version.getVersionString();
    tasks.create(installDependencies, InstallDependencies.class, new Action<InstallDependencies>() {
      @Override
      public void execute(InstallDependencies task) {
        task.dependsOn(createVirtualEnv);
        task.setPythonToolChain(binary.getToolChain());
        task.setPythonBuilDir(binary.getPythonBuildDir());
        task.setVenvDir(binary.getVirtualEnvDir());
      }
    });

    String postFix = capitalize(binary.getName());
    String createWheel = "create" + postFix;
    tasks.create(createWheel, new Action<Task>() {
      @Override
      public void execute(Task task) {
        task.dependsOn(installDependencies);
      }
    });
  }

//    @Mutate
//    void createSampleLibraryComponents(ModelMap<PythonComponentSpec> componentSpecs) {
//        componentSpecs.create("python");
//    }

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
