package com.linkedin.gradle.python.tasks.internal.utilities;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipOutputStreamProcessor extends OutputStream {

    private static final Logger logger = Logging.getLogger(PipOutputStreamProcessor.class);
    private static final Pattern installingPackage = Pattern.compile("Installing collected packages: (.*)");
    private static final Pattern installedPackage = Pattern.compile("Successfully installed (.*)");

    StringBuilder wholeTextBuilder = new StringBuilder();
    StringBuilder lineBuilder = new StringBuilder();

    private final Set<String> packages = new HashSet<String>();

    @Override
    public void write(int b) throws IOException {
        wholeTextBuilder.append((char)b);
        lineBuilder.append((char)b);
        if(b == '\n') {
            processLine(lineBuilder.toString());
            lineBuilder = new StringBuilder();
        }
    }

    private void processLine(String line) {
        Matcher installingPackageMatcher = installingPackage.matcher(line);
        if(installingPackageMatcher.find()) {
            logger.lifecycle("Installing {}", installingPackageMatcher.group(1));
            packages.add(installingPackageMatcher.group(1));
        }

        Matcher installedPackageMatcher = installedPackage.matcher(line);
        if(installedPackageMatcher.find()) {
            logger.lifecycle("Successfully installed {}", installedPackageMatcher.group(1));
        }
    }

    public String getWholeText() {
        return wholeTextBuilder.toString();
    }

    public Set<String> getPackages() {
        return Collections.unmodifiableSet(packages);
    }

    public void addCommand(String join) {
        wholeTextBuilder.append(join).append("\n");
    }
}
