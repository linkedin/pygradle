package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.spec.PythonComponentSpec;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.model.internal.type.ModelType;

import javax.inject.Inject;


public class PythonLangPlugin implements Plugin<Project>  {

    private final ModelRegistry modelRegistry;

    @Inject
    public PythonLangPlugin(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public void apply(final Project project) {
        project.getPluginManager().apply(PythonRulePlugin.class);
        modelRegistry.getRoot().applyToAllLinksTransitive(ModelType.of(PythonComponentSpec.class), PythonBinaryRules.class);
        project.getExtensions().create("pythonConfigurations", PythonPluginConfigurations.class, project.getConfigurations(), project.getDependencies());

        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivyArtifactRepository) {
                ivyArtifactRepository.setName("pipy-cache");
                ivyArtifactRepository.setUrl("http://localhost:8000");
                ivyArtifactRepository.layout("pattern", new OrgModuleRevision());
            }
        });
    }

    public class OrgModuleRevision implements Action<IvyPatternRepositoryLayout> {
        @Override
        public void execute(IvyPatternRepositoryLayout ivyRepositoryLayout) {
            ivyRepositoryLayout.ivy("[organisation]/[module]/[revision]/ivy.xml");
            ivyRepositoryLayout.artifact("[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]");
            ivyRepositoryLayout.setM2compatible(true);
        }
    }
}
