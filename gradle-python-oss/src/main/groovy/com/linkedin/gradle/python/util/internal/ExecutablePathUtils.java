package com.linkedin.gradle.python.util.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ExecutablePathUtils {

    public static File getExecutable(String exeName) {
        for (File dir : getPath()) {
            File candidate = new File(dir, exeName);
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }

    public static List<File> getPath() {
        String path = System.getenv("PATH");
        if (path == null) {
            return Collections.emptyList();
        }
        List<File> entries = new ArrayList<>();
        for (String entry : path.split(Pattern.quote(File.pathSeparator))) {
            entries.add(new File(entry));
        }
        return entries;
    }

}
