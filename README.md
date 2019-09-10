# PyGradle

[![CircleCI](https://img.shields.io/circleci/project/github/linkedin/pygradle.svg?style=flat-square)](https://circleci.com/gh/linkedin/pygradle)
[![Travis](https://img.shields.io/travis/linkedin/pygradle.svg?style=flat-square)](https://travis-ci.org/linkedin/pygradle)
[![AppVeyor](https://img.shields.io/appveyor/ci/ethankhall/pygradle.svg?style=flat-square)](https://ci.appveyor.com/project/ethankhall/pygradle)
[![Bintray](https://img.shields.io/bintray/v/linkedin/maven/pygradle-plugin.svg?style=flat-square)](https://bintray.com/linkedin/maven/pygradle-plugin)
[![Linkedin](https://img.shields.io/badge/opensource-linkedin-blue.svg?style=flat-square)](https://engineering.linkedin.com/)
[![license](https://img.shields.io/github/license/linkedin/pygradle.svg?style=flat-square)](LICENSE)

PyGradle is an enterprise Python build system.

PyGradle leverages [Gradle](https://gradle.org/) to empower Python's existing
ecosystem to solve problems like dependency management, polyglot projects, and
[lots more](https://engineering.linkedin.com/blog/2016/08/introducing--py-gradle--an-open-source-python-plugin-for-gradle).
LinkedIn has been using PyGradle for several years to successfully manage
thousands of Python dependencies.

PyGradle produces artifacts, e.g., source distributions, that are forward and
backward compatible with artifacts produced by vanilla
[setuptools](https://setuptools.readthedocs.io/en/latest/), so there is nothing
stopping you from using PyGradle with one, two, or all of your Python projects
right away!

As a general philosophy, we strive to enhance Python rather than replace it,
thereby keeping open source Python development idiomatic and intuitive.

# Usage

PyGradle does not include batteries: there are a few things that you're going
to have to do to use PyGradle for things beyond our provided [examples](examples).

In particular, you're going to have to:

- Decide how you want to host your artifacts.
- Decide how you want to distribute a custom setuptools distribution class.
- Author your build.gradle file.

These decisions and tasks shouldn't take too long for you finish, but they are
required. A deeper dive into what it takes to get started, how you can use our
demonstrative Artifactory instance, and a few project examples is available in
our detailed [getting started](docs/getting-started.md) guide.

Although not required, if you've never used Gradle before, you might
find reading Gradle's [user guide](https://docs.gradle.org/current/userguide/userguide.html)
helpful.

## Plugins

PyGradle comes with several plugins available, for specific details on each
plugin check the documentation specific to that plugin.

| Plugin                                                        | Usage                                         |
|---------------------------------------------------------------|-----------------------------------------------|
| [com.linkedin.python](docs/plugins/python.md)                 | Extending PyGradle                            |
| [com.linkedin.python-sdist](docs/plugins/python-sdist.md)     | Developing Source Distributions               |
| [com.linkedin.python-web-app](docs/plugins/python-web-app.md) | Developing Flask/Gunicorn Web Applications    |
| [com.linkedin.python-cli](docs/plugins/python-cli.md)         | Developing Command Line Applications          |
| [com.linkedin.python-flyer](docs/plugins/python-flyer.md)     | Developing Flyer (Flask + Ember) Applications |
| [com.linkedin.python-pex](docs/plugins/python-pex.md)         | Developing Pex Applications                   |

# Developers

We're actively maintaining PyGradle and accepting pull requests.

If you're interested in contributing code to PyGradle, please see our
[developers](docs/developers.md) document to get started. This document covers
how to build, test, and debug PyGradle, and kindly asks that you follow a few
conventions when submitting pull requests.

# Compatibility

## Software

We support PyGradle on the following software versions.

| Software | Version |
|----------|---------|
| Java     | 8.0     |
| Gradle   | 5.0     |

We're happy to review and merge pull requests that add additional support to
future versions of software.

## Platforms

PyGradle works on the following platforms.

| Platform  | Support    |
|-----------|------------|
| Linux     | Full       |
| OS X      | Full       |
| Windows   | Partial    |

Additional compatibility notes are availabe [here](docs/compatibility.md).
We're happy to review and merge pull requests that add support for additional
platforms.
