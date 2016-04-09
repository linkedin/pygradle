package com.linkedin.gradle.python.tasks.internal.utilities;

import com.linkedin.gradle.python.internal.platform.PythonVersion;

import java.io.File;
import java.util.List;

public class TaskUtils {

    public static String join(List<String> args, String separator) {
        if (args == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i));
            if (i != args.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static File sitePackage(File vendDir, PythonVersion version) {
        String sitePackagePath = String.format("lib/python%s/site-packages", version.getMajorMinorVersion());
        return new File(vendDir, sitePackagePath);
    }
}
