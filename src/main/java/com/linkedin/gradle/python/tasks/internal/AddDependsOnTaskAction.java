package com.linkedin.gradle.python.tasks.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;


public class AddDependsOnTaskAction implements Action<Task> {

  private final Object dependsOn;

  public AddDependsOnTaskAction(Object dependsOn) {
    this.dependsOn = dependsOn;
  }

  @Override
  public void execute(Task task) {
    task.dependsOn(dependsOn);
  }
}
