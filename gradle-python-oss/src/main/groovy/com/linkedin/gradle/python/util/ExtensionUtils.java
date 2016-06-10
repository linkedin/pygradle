package com.linkedin.gradle.python.util;

import com.linkedin.gradle.python.PythonExtension;
import com.linkedin.gradle.python.extension.CliExtension;
import com.linkedin.gradle.python.extension.DeployableExtension;
import com.linkedin.gradle.python.extension.PexExtension;
import com.linkedin.gradle.python.extension.WheelExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

public class ExtensionUtils {

    public static <T> T maybeCreate(PythonExtension component, String name, Class<T> type, Object... args) {
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();

        T maybeExtension = extensionContainer.findByType(type);
        if(maybeExtension == null) {
            maybeExtension = extensionContainer.create(name, type, args);
        }
        return maybeExtension;
    }

    public static DeployableExtension maybeCreateDeployableExtension(Project project) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        return maybeCreate(component, "deployable", DeployableExtension.class, project);
    }

    public static PexExtension maybeCreatePexExtension(Project project) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        return maybeCreate(component, "pex", PexExtension.class, project);
    }

    public static WheelExtension maybeCreateWheelExtension(Project project) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        return maybeCreate(component, "wheel", WheelExtension.class, project);
    }

    public static CliExtension maybeCreateCliExtension(Project project) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        return maybeCreate(component, "cli", CliExtension.class);
    }

    public static <T> T getPythonComponentExtension(Project project, Class<T> type) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();
        return extensionContainer.getByType(type);
    }

    public static <T> T findPythonComponentExtension(Project project, Class<T> type) {
        PythonExtension component = project.getExtensions().getByType(PythonExtension.class);
        ExtensionContainer extensionContainer = ((ExtensionAware) component).getExtensions();
        return extensionContainer.findByType(type);
    }

    public static PythonExtension getPythonExtension(Project project) {
        return project.getExtensions().getByType(PythonExtension.class);
    }
}
