package com.linkedin.gradle.python.plugin.internal;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.PythonTestSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonTestSourceSet;
import com.linkedin.gradle.python.plugin.PythonPluginConfigurations;
import com.linkedin.gradle.python.spec.component.PythonComponentSpec;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.language.base.internal.BuildDirHolder;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;
import org.gradle.platform.base.internal.BinaryNamingSchemeBuilder;
import org.gradle.platform.base.internal.DefaultBinaryNamingSchemeBuilder;


@SuppressWarnings("unused")
public class BasePythonRulePlugin extends RuleSource {

    @LanguageType
    public void registerLanguage(LanguageTypeBuilder<PythonSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonSourceSet.class);
    }

    @LanguageType
    public void registerLanguageTests(LanguageTypeBuilder<PythonTestSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonTestSourceSet.class);
    }

    @Model
    public BinaryNamingSchemeBuilder binaryNamingSchemeBuilder() {
        return new DefaultBinaryNamingSchemeBuilder();
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
                wheelComponentSpec.getSources().create("pythonTest", PythonTestSourceSet.class, new Action<PythonTestSourceSet>() {
                    @Override
                    public void execute(PythonTestSourceSet pythonTestSourceSet) {
                        pythonTestSourceSet.getSource().srcDir("src/test/python");
                        pythonTestSourceSet.getSource().include("**/*.py");
                    }
                });
            }
        });
    }
}
