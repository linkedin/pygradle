package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.PythonSourceSet;
import com.linkedin.gradle.python.internal.DefaultPythonSourceSet;
import com.linkedin.gradle.python.spec.DefaultPythonComponentSpec;
import com.linkedin.gradle.python.spec.DefaultWheelBinarySpec;
import com.linkedin.gradle.python.spec.PythonComponentSpec;
import com.linkedin.gradle.python.spec.WheelBinarySpec;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
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

    @ComponentType
    public void register(ComponentTypeBuilder<PythonComponentSpec> builder) {
        builder.defaultImplementation(DefaultPythonComponentSpec.class);
    }

    @BinaryType
    public void registerJar(BinaryTypeBuilder<WheelBinarySpec> builder) {
        builder.defaultImplementation(DefaultWheelBinarySpec.class);
    }

    @ComponentBinaries
    public void createBinaries(ModelMap<WheelBinarySpec> binaries,
                               final PythonComponentSpec pythonComponent) {

        binaries.create(pythonComponent.getName() + ".whl");
    }

    @BinaryTasks
    public void createTasks(ModelMap<Task> tasks, final WheelBinarySpec binary) {
        tasks.create("buildWheel");
    }

    @Mutate
    void createSampleLibraryComponents(ModelMap<PythonComponentSpec> componentSpecs) {
        componentSpecs.create("python");
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
