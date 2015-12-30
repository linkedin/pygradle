package com.linkedin.gradle.python.tasks.internal;

import com.linkedin.gradle.python.tasks.internal.utilities.PipInstallHelper;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.incremental.InputFileDetails;


public class InstallDependenciesIncrementalAction implements Action<InputFileDetails> {

  private static final Logger logger = Logging.getLogger(InstallDependenciesIncrementalAction.class);
  private final PipInstallHelper pipInstallHelper;

  public InstallDependenciesIncrementalAction(PipInstallHelper pipInstallHelper) {
    this.pipInstallHelper = pipInstallHelper;
  }

  @Override
  public void execute(InputFileDetails inputFileDetails) {
    logger.info("Updating {}", inputFileDetails.getFile().getName());
    if(inputFileDetails.isRemoved()) {
      pipInstallHelper.uninstall(inputFileDetails.getFile());
    }
    if(inputFileDetails.isModified()) {
      pipInstallHelper.forceInstall(inputFileDetails.getFile());
    }
    if (inputFileDetails.isAdded()) {
      pipInstallHelper.install(inputFileDetails.getFile());
    }
  }
}
