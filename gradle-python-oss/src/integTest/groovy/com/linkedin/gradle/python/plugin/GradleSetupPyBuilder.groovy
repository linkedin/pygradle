package com.linkedin.gradle.python.plugin;

class GradleSetupPyBuilder {

    public static String createSetupPy() {
        return '''\
            | import setuptools
            |
            | from distgradle import GradleDistribution
            |
            |
            | setuptools.setup(
            |     distclass=GradleDistribution,
            |     package_dir={'': 'src'},
            |     packages=setuptools.find_packages('src'),
            |     include_package_data=True,
            |     entry_points={
            |         'console_scripts': [
            |             'hello_world = hello:main',
            |         ],
            |     },
            | )
            '''.stripMargin().stripIndent()
    }
}