package com.linkedin.gradle.python.tasks.internal.utilities;

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
    void processLine(String line) {
        //Implemented by users who need this
    }

    public String getWholeText() {
        return wholeTextBuilder.toString();
    }

    public void addCommand(String join) {
        wholeTextBuilder.append(join).append("\n");
    }
}
