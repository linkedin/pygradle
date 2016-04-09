package com.linkedin.gradle.python.plugin;

import com.linkedin.gradle.python.spec.binary.PythonBinarySpec;
import com.linkedin.gradle.python.spec.binary.internal.PythonBinarySpecInternal;
import com.linkedin.gradle.python.tasks.PublishingTask;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;


public class PythonIvyPublishingPlugin implements Plugin<Project> {

    private static final Logger logger = Logging.getLogger(PythonIvyPublishingPlugin.class);

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(PythonBaseLangPlugin.class);
        project.getPluginManager().apply(Rules.class);
    }

    public static class Rules extends RuleSource {

        @Mutate
        public void publishBinaries(PublishingExtension publishingExtension, ModelMap<PythonBinarySpec> specs) {
            final PublicationContainer publications = publishingExtension.getPublications();
            for (final PythonBinarySpecInternal spec : specs.withType(PythonBinarySpecInternal.class)) {
                if (publications.findByName(spec.getName()) != null) {
                    continue;
                }
                for (PublishingTask publishingTask : spec.getTasks().withType(PublishingTask.class)) {
                    publications.create(spec.getName(), IvyPublication.class, new IvyPublishAction(publishingTask));
                }
            }
        }
    }

    private static class IvyPublishAction implements Action<IvyPublication> {

        private final PublishingTask buildTask;

        private IvyPublishAction(PublishingTask buildTask) {
            this.buildTask = buildTask;
        }

        @Override
        public void execute(IvyPublication ivyPublication) {
            PublishArtifact artifact = buildTask.getFileToPublish();
            ivyPublication.artifact(artifact);
        }
    }
}
