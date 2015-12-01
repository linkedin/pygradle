package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.PythonPlatformResolver;
import com.linkedin.gradle.python.internal.platform.DefaultPythonToolChainRegistry;
import com.linkedin.gradle.python.internal.platform.PythonToolChainRegistry;
import com.linkedin.gradle.python.plugin.PythonLangPlugin;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.component.internal.PythonComponentSpec;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.*;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;
import org.gradle.platform.base.internal.BinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.DefaultBinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.PlatformResolvers;
import org.gradle.process.internal.ExecActionFactory;

import java.io.File;


@SuppressWarnings("unused")
public class BasePythonRulePlugin extends RuleSource {
    public static final Logger log = Logging.getLogger(PythonLangPlugin.class);

    @LanguageType
    public void registerLanguage(LanguageTypeBuilder<PythonSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonSourceSet.class);
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
    void createPythonSourceSets(ModelMap<PythonComponentSpec> binaries) {
        binaries.all(new Action<PythonComponentSpec>() {
            @Override
            public void execute(PythonComponentSpec wheelComponentSpec) {
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
