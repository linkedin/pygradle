package com.linkedin.gradle.python.tasks;

import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;


public interface PublishingTask extends Task {
    PublishArtifact getFileToPublish();
}
