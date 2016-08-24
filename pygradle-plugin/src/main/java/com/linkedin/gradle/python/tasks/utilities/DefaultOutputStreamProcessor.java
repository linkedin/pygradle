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

package com.linkedin.gradle.python.tasks.utilities;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Stream processor for capturing output and writing more useful output.
 * <p>
 * After each line, {@link #processLine(String)} will be called, allowing the extenders to be able to as lines happen,
 * write useful information out.
 */
public class DefaultOutputStreamProcessor extends OutputStream {

    private StringBuilder wholeTextBuilder = new StringBuilder();
    private StringBuilder lineBuilder = new StringBuilder();

    @Override
    public void write(int b)
            throws IOException {
        wholeTextBuilder.append((char) b);
        lineBuilder.append((char) b);
        if (b == '\n') {
            processLine(lineBuilder.toString());
            lineBuilder = new StringBuilder();
        }
    }

    /**
     * Called when a line end is detected.
     *
     * @param line The while line.
     */
    public void processLine(String line) {
        //Implemented by users who need this
    }

    public String getWholeText() {
        return wholeTextBuilder.toString();
    }

    public void addCommand(String join) {
        wholeTextBuilder.append(join).append("\n");
    }
}
