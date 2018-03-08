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
package com.linkedin.gradle.python.tasks.execution;

import org.apache.commons.io.output.TeeOutputStream;
import org.gradle.process.ExecSpec;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Collects all the output of commands so they can be used elsewhere
 */
public class TeeOutputContainer {

    private final ByteArrayOutputStream mergedStream = new ByteArrayOutputStream();
    private final OutputStream teeStdOut;
    private final OutputStream teeErrOut;

    public TeeOutputContainer(OutputStream stdOut, OutputStream errOut) {
        teeStdOut = new TeeOutputStream(stdOut, mergedStream);
        teeErrOut = new TeeOutputStream(errOut, mergedStream);
    }

    public TeeOutputContainer() {
        this(System.out, System.err);
    }


    public void setOutputs(ExecSpec execSpec) {
        execSpec.setStandardOutput(teeStdOut);
        execSpec.setErrorOutput(teeErrOut);
    }

    public String getCommandOutput() {
        return mergedStream.toString();
    }
}
