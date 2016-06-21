# About
The PyGradle build system is a set of Gradle plugins that can be used to build Python artifacts. Artifacts produced by
PyGradle are forward and backward compatible with artifacts produced with Python's setuptools library.

## Why should I use PyGradle?

Although most aspects of the Python ecosystem are exceptional, there are some aspects that aren't so great. The Python
ecosystem is extremely productive in aspects such as local development, artifact management, library functionality, and
more. But, the Python ecosystem does struggle with things like dependency management, dependency resolution, conflict
resolution, integration with existing metadata systems, and more. Projects that are built with the PyGradle build system
get the best of both worlds by leveraging each tool, Python and Gradle, only for what it is good at.

The @linkedin/pygradle-devs team feels the major advantages of using PyGradle, among many others, are the following.

- You get real dependency resolution as opposed to mock dependency resolution.
- You get conflict resolution which resolves the notorious VersionConflict and DistributionNotFound errors that plague large projects.
- You get the Gradle cache which is an enterprise quality caching solution that many organizations leverage.
- You get the ability to build your Python code alongside your Java, Scala, and C++ code in a first class way.
- You get a pluggable build system that can quickly be customized or adapted to support new technologies like pex.
- You get to integrate completely with the metadata systems that teams have spent millions of engineering dollars working on.

# Getting Started

*This section will be updated once we release the plugin into gradle's plugin repository.*

For a quick start, lets look at a simple example of publishing a library using two dependencies.

    apply plugin: 'python-sdist'

    dependencies {
        python 'pypi:requests:2.5.1'
        test 'pypi:mock:1.0.1'
    }

We apply a plugin `python-sdist` which adds configurations `python` and `test` to the project. In the dependencies section
we add two dependencies, one required to run and one required for testing. With this information PyGradle will download the
artifacts from a repository (omitted from example) install requests and mock along with their dependencies to a virtual
environment and run any tests that you may have.

## Plugins Avalible

PyGradle comes with several plugins avalible, for specific details on plugins check the documentation specific to that plugin.

| Plugin Name       | Used When                                     | Documentation |
|-------------------|-----------------------------------------------|---------------|
| python-sdist      | Developing Libraries                          | TODO          |
| python-web-app    | Developing Deployable Applications            | TODO          |
| python-cli        | Developing Command Line Applications          | TODO          |
| python-flyer      | Developing Flyer (Flask + Ember) Applications | TODO          |
| python-pex        | Developing Pex Applications                   | TODO          |

# Developing on PyGradle

## Building PyGradle

To build PyGradle run `./gradlew build`. This will compile the project, run the tests and integration tests.

To publish to a local repo, run `./gradlew publishToMavenLocal`. This will publish PyGradle artifacts to ~/.m2 using maven as
the metadata format. To use this version, be sure to update the version in the project under test and add `mavenLocal()` to the
repositories.

## Contributing

To contribute to PyGradle please fork the project, make your changes locally and open a pull request. If possible include a
description about why this change is being added along with tests that validate the changes. Your commits must pass checkstyle
and codenarc.

We will be adding an automated CI system so that pull requests will be validated automatically.

From time to time LinkedIn maintainers may find issues with the changes that break our internal tests. When this happens we will
allow the merge to happen but add tests and update the code to make our internal tests pass. This means that we probably won't
publish a version until we can validate the changes internally.
