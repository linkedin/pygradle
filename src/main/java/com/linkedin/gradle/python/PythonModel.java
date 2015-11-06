package com.linkedin.gradle.python;

import org.gradle.model.Managed;

@Managed
public interface PythonModel {

    void setLanguageLevel(String languageLevel);
    String getLanguageLevel();

}
