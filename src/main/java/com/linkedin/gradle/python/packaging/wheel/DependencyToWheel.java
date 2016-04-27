/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.gradle.python.packaging.wheel;

import com.linkedin.gradle.python.packaging.PackageInformation;
import com.linkedin.gradle.python.packaging.PythonPackageNamingUtil;
import com.linkedin.gradle.python.spec.component.PythonEnvironment;
import com.linkedin.gradle.python.utils.OutputUtilities;
import java.io.File;
import java.util.regex.Pattern;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;


public class DependencyToWheel {
  private final static Logger logger = Logging.getLogger(DependencyToWheel.class);

  private final File wheelCache;
  private final PythonEnvironment pythonEnvironment;

  public DependencyToWheel(File wheelCache, PythonEnvironment pythonEnvironment) {
    this.wheelCache = wheelCache;
    this.pythonEnvironment = pythonEnvironment;
  }

  public void convertToWheel(File dependency) {
    PackageInformation packageInformation = PythonPackageNamingUtil.packageInfoFromPath(dependency.getAbsolutePath());
    String shortHand = packageInformation.toShortHand();
    final Pattern fileMatcher = Pattern.compile(
        String.format(".*%s%s-%s-.*\\.whl", File.separator, packageInformation.getName().replace("-", "_"),
            packageInformation.getVersion()));

    File[] files = wheelCache.listFiles(pathname -> fileMatcher.matcher(pathname.getAbsolutePath()).find());

    if (files.length != 0) {
      logger.lifecycle(OutputUtilities.writePaddedString("Using cached wheel for " + shortHand, OutputUtilities.SKIPPED_INSTALL_MESSAGE));
      return;
    }

    DependencyToWheelAction wheelAction = new DependencyToWheelAction(pythonEnvironment.getVenvDir(), wheelCache, dependency);
    ExecResult installResult = pythonEnvironment.getVirtualEnvPythonExecutable().execute(wheelAction);

    if (installResult.getExitValue() != 0) {
      logger.lifecycle(wheelAction.getOutputStream().getWholeText());
      throw new GradleException("Failed to build wheel for " + shortHand
          + ". Please see above output for reason, or re-run your build using ``--info`` for additional logging.");
    } else {
      logger.info(wheelAction.getOutputStream().getWholeText());
      logger.lifecycle(OutputUtilities.writePaddedString("Built wheel for " + shortHand, OutputUtilities.GOOD_INSTALL_MESSAGE));
    }
  }
}
