[![CircleCI](https://circleci.com/gh/linkedin/pygradle.svg?style=svg)](https://circleci.com/gh/linkedin/pygradle) [ ![Download](https://api.bintray.com/packages/ethankhall/pygradle/pygradle/images/download.svg) ](https://bintray.com/ethankhall/pygradle/pygradle/_latestVersion)

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

- You get real dependency resolution as opposed to 'mock' dependency resolution.
- You get conflict resolution which resolves the notorious VersionConflict and DistributionNotFound errors that plague large projects.
- You get the Gradle cache which is an enterprise quality caching solution that many organizations leverage.
- You get the ability to build your Python code alongside your Java, Scala, and C++ code in a first class way.
- You get a pluggable build system that can quickly be customized or adapted to support new technologies like pex.
- You get to integrate completely with the metadata systems that teams have spent millions of engineering dollars working on.

# Getting Started

For a quick start, lets look at a simple example of publishing a library using two dependencies.

    plugins {
      id "com.linkedin.python-sdist" version "0.3.7"
    }

    dependencies {
        python 'pypi:requests:2.5.1'
        test 'pypi:mock:1.0.1'
    }
    
    repositories {
       pyGradlePyPi()
    }

We apply a plugin `com.linkedin.python-sdist` which adds configurations `python` and `test` to the project. In the dependencies section
we add two dependencies, one required to run and one required for testing. With this information PyGradle will download the
artifacts from a repository (omitted from example) install requests and mock along with their dependencies to a virtual
environment and run any tests that you may have.

For a more step by step guide, check out the example project in
[examples/](examples)

## Plugins Available

PyGradle comes with several plugins available, for specific details on each plugin check the documentation specific to that plugin.

| Plugin Name                                           | Used When                                     |
|-------------------------------------------------------|-----------------------------------------------|
| [com.linkedin.python](docs/python.md)                 | Base Python Plugin                            |
| [com.linkedin.python-sdist](docs/python-sdist.md)     | Developing Libraries                          |
| [com.linkedin.python-web-app](docs/python-web-app.md) | Developing Deployable Applications            |
| [com.linkedin.python-cli](docs/python-cli.md)         | Developing Command Line Applications          |
| [com.linkedin.python-flyer](docs/python-flyer.md)     | Developing Flyer (Flask + Ember) Applications |
| [com.linkedin.python-pex](docs/python-pex.md)         | Developing Pex Applications                   |

## Custom Setup Tools Distribution Class

There are some cases where you will need to implement a distribution class that can take the command 'entrypoints'. We provide
a suggested setup.py for projects. You can find it in pygradle-plugin/templates/setup.py.template. In order to make it easy for
consumers to use, we also provide a task `generateSetupPy` that will write it out to disk. Be careful, this task *will* 
overwrite any existing setup.py in the project.

## PyPi Artifacts

PyGradle depends on Ivy metadata to build a dependency graph, thus we cannot use pypi directly. We do have a java library that 
will convert libraries from pypi into Ivy located in pivy-importer. In that project you can find usage examples. The pivy-importer
project is how the integration tests get their required dependencies.

To help with on boarding, we are providing a repository that has all of pygradle's require dependencies. You can apply it by adding 

    repositories {
       pyGradlePyPi()
    }

to your build.gradle. This is not intended to be a full mirror and will never be a full mirror of pypi. It has the dependencies that
PyGradle requires to start, and may in the future include very common libraries. The repo was seeded using the pivy-importer.

For more details on the pivy-importer please [read the docs](docs/pivy-importer.md).

# Developing on PyGradle

## Building PyGradle

To build PyGradle run `./gradlew build`. This will compile the project, run the tests and integration tests.

To publish to a local repo, run `./gradlew publishToMavenLocal`. This will publish PyGradle artifacts to ~/.m2 using maven as
the metadata format. To use this version, be sure to update the version in the project under test and add `mavenLocal()` to the
repositories.

## Contributing

To contribute to PyGradle please fork the project, make your changes locally and open a pull request. If possible include a
description about why this change is being added along with tests that validate the changes. Your commits must pass checkstyle
and codenarc. Any substantial change should include unit/integration tests.

A CI build runs with every pull request, the build must pass before we will merge any commits.

We prefer that you squash commits into a single commit for a single change, multiple changes may be multiple commits.

From time to time LinkedIn maintainers may find issues with the changes that break our internal tests. When this happens we will
allow the merge to happen but add tests and update the code to make our internal tests pass. This means that we probably won't
publish a version until we can validate the changes internally.

# Known Potential Issues
- Due to a bug in pip, when trying to install scipy may fail. A potential work around is to use a newer version of pip. A PR
was merged into pip master that fixes the issue (https://github.com/pypa/pip/pull/3701), a version of pip with the fix in it
has not been released yet. If this is an issue for your org, you could release a version of pip with this fix in it. For
more details on the change and issues please review https://github.com/pypa/pip/pull/3701 and https://github.com/pypa/pip/pull/3079
