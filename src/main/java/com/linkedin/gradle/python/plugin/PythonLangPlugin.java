package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.spec.PythonComponentSpec;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.artifacts.repositories.RepositoryLayout;
import org.gradle.api.internal.artifacts.repositories.layout.IvyRepositoryLayout;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.model.internal.type.ModelType;


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
                ivyArtifactRepository.setName("pypi-external");
                ivyArtifactRepository.setUrl("http://artifactory.corp.linkedin.com:8081/artifactory/pypi-external");
                ivyArtifactRepository.layout("pattern", new Action<IvyPatternRepositoryLayout>() {
                    @Override
                    public void execute(IvyPatternRepositoryLayout repositoryLayout) {
                        repositoryLayout.ivy("[module]/[revision]/[module]-[revision].ivy");
                        repositoryLayout.artifact("[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]");
                        repositoryLayout.setM2compatible(true);
                    }
                });
            }
        });

        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivyArtifactRepository) {
                ivyArtifactRepository.setName("pypi-internal");
                ivyArtifactRepository.setUrl("http://artifactory.corp.linkedin.com:8081/artifactory/pypi-internal");
                ivyArtifactRepository.layout("pattern", new OrgModuleRevision());
            }
        });

        project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivyArtifactRepository) {
                ivyArtifactRepository.setName("TOOLS");
                ivyArtifactRepository.setUrl("http://artifactory.corp.linkedin.com:8081/artifactory/TOOLS");
                ivyArtifactRepository.layout("pattern", new OrgModuleRevision());
            }
        });
    }

    public class OrgModuleRevision implements Action<IvyPatternRepositoryLayout> {
        @Override
        public void execute(IvyPatternRepositoryLayout ivyRepositoryLayout) {
            ivyRepositoryLayout.ivy("[organisation]/[module]/[revision]/[module]-[revision].ivy");
            ivyRepositoryLayout.artifact("[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]");
            ivyRepositoryLayout.setM2compatible(true);
        }
    }
}
