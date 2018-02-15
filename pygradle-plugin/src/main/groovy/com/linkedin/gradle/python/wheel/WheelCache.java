package com.linkedin.gradle.python.wheel;

import com.linkedin.gradle.python.extension.PlatformTag;
import com.linkedin.gradle.python.extension.PythonTag;
import com.linkedin.gradle.python.extension.PythonVersion;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class WheelCache implements Serializable {

    private static final Logger logger = Logging.getLogger(WheelCache.class);

    private final File cacheDir;
    private final PythonTag pythonTag;
    private final PlatformTag platformTag;

    public WheelCache(File cacheDir, PythonTag pythonTag, PlatformTag platformTag) {
        this.cacheDir = cacheDir;
        this.pythonTag = pythonTag;
        this.platformTag = platformTag;
    }

    public Optional<File> findWheel(String library, String version, PythonVersion pythonVersion) {

        if(cacheDir == null) {
            return Optional.empty();
        }

        File[] files = cacheDir.listFiles((dir, name) ->
            name.startsWith(library.replace("-", "_") + "-" + version) && name.endsWith(".whl"));

        if (files == null) {
            return Optional.empty();
        }

        Set<String> pythonAbi = new HashSet<>(Collections.singleton("py2.py3"));
        pythonAbi.add("py" + pythonVersion.getPythonMajor());
        pythonAbi.add("py" + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor());
        pythonAbi.add(pythonTag.getPrefix() + pythonVersion.getPythonMajor() + pythonVersion.getPythonMinor());

        List<String> platformTag = Arrays.asList("any.whl", this.platformTag.getPlatform() + ".whl");

        logger.debug("Searching for {} {} with options: {}, {}", library, version, pythonAbi, platformTag);

        List<File> collect = Arrays.stream(files)
            .filter(file -> pythonAbi.stream().anyMatch(it -> file.getName().contains(it)))
            .filter(file -> platformTag.stream().anyMatch(it -> file.getName().endsWith(it)))
            .collect(Collectors.toList());

        if (collect.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(collect.get(0));
        }
    }

    public File getCacheDir() {
        return cacheDir;
    }
}
