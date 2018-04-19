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
package com.linkedin.gradle.python.exception;

import com.linkedin.gradle.python.util.PackageInfo;
import org.gradle.api.GradleException;

import java.util.List;

public class PipExecutionException extends GradleException {
    private final String pipText;

    private PipExecutionException(String message, String pipText) {
        super(message);
        this.pipText = pipText;
    }

    public String getPipText() {
        return pipText;
    }

    public static PipExecutionException failedInstall(PackageInfo packageInfo, String pipText) {
        String body = String.format("Failed to install %s. "
            + "Please see above output for reason, or re-run your build using "
            + "``gradle -i build`` for additional logging.", packageInfo.toShortHand());
        return new PipExecutionException(body, pipText);
    }

    public static PipExecutionException failedWheel(PackageInfo packageInfo, String pipText) {
        String body = String.format("Failed to build wheel for %s. "
            + "Please see above output for reason, or re-run your build using "
            + "``gradle -i build`` for additional logging.", packageInfo.toShortHand());
        return new PipExecutionException(body, pipText);
    }

    public static PipExecutionException unsupportedPythonVersion(PackageInfo packageInfo, List<String> supportedVersions) {
        String message = String.format("Package %s works only with Python versions: %s", packageInfo.getName(), supportedVersions);
        return new PipExecutionException(message, message);
    }
}
