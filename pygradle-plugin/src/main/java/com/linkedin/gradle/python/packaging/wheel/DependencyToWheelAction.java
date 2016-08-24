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

import com.linkedin.gradle.python.tasks.utilities.DefaultOutputStreamProcessor;
import java.io.File;
import org.gradle.api.Action;
import org.gradle.process.internal.ExecAction;


public class DependencyToWheelAction implements Action<ExecAction> {

  private final DefaultOutputStreamProcessor os = new DefaultOutputStreamProcessor();
  private final File venvDir;
  private final File wheelCache;
  private final File dependency;

  public DependencyToWheelAction(File venvDir, File wheelCache, File dependency) {
    this.venvDir = venvDir;
    this.wheelCache = wheelCache;
    this.dependency = dependency;
  }

  @Override
  public void execute(ExecAction execAction) {
    //@formatter:off
    execAction.args(new File(venvDir, "bin/pip").getAbsolutePath(),
        "wheel",
        "--disable-pip-version-check",
        "--wheel-dir", wheelCache.getAbsolutePath(),
        "--no-deps",
        dependency.getAbsolutePath());
    //@formatter:on

    execAction.setStandardOutput(os);
    execAction.setErrorOutput(os);
    execAction.setIgnoreExitValue(true);
  }

  public DefaultOutputStreamProcessor getOutputStream() {
    return os;
  }

}
