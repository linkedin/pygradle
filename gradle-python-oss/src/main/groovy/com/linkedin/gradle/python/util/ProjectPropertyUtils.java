package com.linkedin.gradle.python.util;

import org.gradle.api.Project;

public class ProjectPropertyUtils {

    public static boolean isPropertySet(Project project, String name) {
        if(!project.hasProperty(name)) {
            return false;
        }

        return Boolean.parseBoolean(project.property(name).toString());
    }
}
