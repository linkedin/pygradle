package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.spec.DefaultPythonBinarySpec;
import com.linkedin.gradle.python.spec.DefaultPythonComponent;
import com.linkedin.gradle.python.spec.PythonBinarySpec;
import com.linkedin.gradle.python.spec.PythonComponent;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.language.base.internal.registry.LanguageTransformContainer;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.platform.base.*;

@SuppressWarnings("unused")
public class PythonRulePlugin extends RuleSource {
    public static final Logger log = Logging.getLogger(PythonLangPlugin.class);

    @LanguageType
    public void registerLanguage(LanguageTypeBuilder<PythonSourceSet> builder) {
        builder.setLanguageName("python");
        builder.defaultImplementation(DefaultPythonSourceSet.class);
    }

    @Mutate
    public void registerLanguageTransform(LanguageTransformContainer languages, ServiceRegistry serviceRegistry) {
        languages.add(new PythonLanguageTransform());
    }

    @ComponentType
    void defineType(ComponentTypeBuilder<PythonComponent> builder) {
        builder.defaultImplementation(DefaultPythonComponent.class);
    }

    @BinaryType
    void registerPythonBinaryType(BinaryTypeBuilder<PythonBinarySpec> builder) {
        builder.defaultImplementation(DefaultPythonBinarySpec.class);
    }

    @ComponentBinaries
    void createBinariesForBinaryComponent(ModelMap<PythonBinarySpec> binaries, PythonComponent library) {
        binaries.create("compilePython", new Action<PythonBinarySpec>() {
            @Override
            public void execute(PythonBinarySpec pythonBinarySpec) {
                log.lifecycle("Creating component");
            }
        });
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks, final PythonBinarySpec executableBinary){
        log.lifecycle("Creating tasks from: {}", executableBinary);
    }
}
