package com.linkedin.gradle.python.tasks.internal.utilities;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipOutputStreamProcessor extends DefaultOutputStreamProcessor {

    private static final Logger logger = Logging.getLogger(PipOutputStreamProcessor.class);
    private static final Pattern INSTALLING_PACKAGE_PATTERN = Pattern.compile("Installing collected packages: (.*)");
    private static final Pattern INSTALLED_PACKAGE_PATTERN = Pattern.compile("Successfully installed (.*)");

    private static final String GOOD_INSTALL_MESSAGE = " [GOOD]";

    private final Set<String> packages = new HashSet<String>();

    void processLine(String line) {
        Matcher installingPackageMatcher = INSTALLING_PACKAGE_PATTERN.matcher(line);
        if (installingPackageMatcher.find()) {
            logger.info("Installing {}", installingPackageMatcher.group(1));
            packages.add(installingPackageMatcher.group(1));
        }

        Matcher installedPackageMatcher = INSTALLED_PACKAGE_PATTERN.matcher(line);
        if (installedPackageMatcher.find()) {
            logger.lifecycle(writeInstalledBuffer(installedPackageMatcher.group(1)));
            logger.info("Successfully installed {}", installedPackageMatcher.group(1));
        }
    }

    private String writeInstalledBuffer(String name) {
        StringBuilder sb = new StringBuilder();
        name = name + " ";
        int bufferSize = Math.max(5, 80 - sb.length() - GOOD_INSTALL_MESSAGE.length());
        sb.append(StringUtils.rightPad(name, bufferSize, '.'));
        sb.append(GOOD_INSTALL_MESSAGE);
        return sb.toString();
    }

    public Set<String> getPackages() {
        return Collections.unmodifiableSet(packages);
    }
}
