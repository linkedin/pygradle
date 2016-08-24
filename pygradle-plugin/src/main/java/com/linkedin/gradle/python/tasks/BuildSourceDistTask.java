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

package com.linkedin.gradle.python.tasks;

import com.linkedin.gradle.python.tasks.internal.AbstractDistTask;
import org.gradle.api.tasks.Input;

import java.io.File;
import java.util.Collections;
import java.util.List;


public class BuildSourceDistTask extends AbstractDistTask {
    public BuildSourceDistTask() {
        super("sdist");
    }

    @Input
    private String outputFormat;

    public List<String> extraArgs() {
        return Collections.singletonList(String.format("--formats=%s", outputFormat));
    }

    @Override
    protected File getPythonArtifact() {
        return new File(distributablePath,
                String.format("%s-%s.%s", getProject().getName(), getProject().getVersion(), getExtension()));
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    @Override
    public String getExtension() {
        if ("gztar".equals(outputFormat)) {
            return "tar.gz";
        } else if ("zip".equals(outputFormat)) {
            return "zip";
        }
        throw new UnknownFileTypeException("Unknown format " + outputFormat);
    }

    public static class UnknownFileTypeException extends RuntimeException {
        public UnknownFileTypeException(String s) {
            super(s);
        }
    }

}
