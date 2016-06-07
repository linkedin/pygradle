package com.linkedin.gradle.python.plugin;

class GradleSetupCfgBuilder {
    public static String createSetupCfg() {
        return '''\
            | [flake8]
            | ignore = E121,E123,E226,W292
            | max-line-length = 160
            |
            | [pytest]
            | addopts = --ignore build/ --ignore dist/
            '''.stripMargin().stripIndent()
    }
}
