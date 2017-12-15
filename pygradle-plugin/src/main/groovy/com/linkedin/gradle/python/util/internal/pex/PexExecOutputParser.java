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
package com.linkedin.gradle.python.util.internal.pex;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.process.ExecResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


class PexExecOutputParser {

    private static final Logger logger = Logging.getLogger(PexExecOutputParser.class);

    private final String outputFromPexCommand;
    private final int returnCode;

    PexExecOutputParser(PexExecSpecAction pexExecSpecAction, ExecResult execResult) {
        this(pexExecSpecAction.getOutputStream().toString().trim(), execResult.getExitValue());
    }

    PexExecOutputParser(String outputFromPexCommand, int returnCode) {
        this.outputFromPexCommand = outputFromPexCommand;
        this.returnCode = returnCode;
    }

    void validatePexBuildSuccessfully() {
        if (returnCode == 0) {
            return;
        }

        String packageName = "<see output above>";

        logger.lifecycle(outputFromPexCommand);
        Pattern pattern = Pattern.compile("(?s).*Could not satisfy all requirements for ([\\w.-]+):.*");
        Matcher matcher = pattern.matcher(outputFromPexCommand);
        if (matcher.matches()) {
            packageName = matcher.group(1);
        }

        String lineSeperator = System.getProperty("line.separator");
        throw new GradleException("Failed to build a pex file (see output above)!" + lineSeperator
            + lineSeperator
            + "This typically happens because your virtual environment contains a cached copy of " + packageName + lineSeperator
            + "that no other package depends on any more." + lineSeperator
            + "Usually, this is the result of updating a package that used to depend on " + packageName + "."
        );
    }

}
