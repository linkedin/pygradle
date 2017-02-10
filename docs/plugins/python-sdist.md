# com.linkedin.python-sdist


## About
The `com.linkedin.python-sdist` plugin is a plugin that builds a source distribution, called an sdist, for your Python project. It is typically used for library type projects but can also be used to package data and static content.

## Usage

```
plugins {
    id 'com.linkedin.python-sdist', version <latest version>
}
```

## Artifacts

The `com.linkedin.python-sdist` plugin should create a single artifact from the build.
The artifact will be named <project name>-<project version>.tar.gz and will be created in the subproject's directory in the dist directory.

```
<project dir>
├── build.gradle
├── setup.cfg
├── setup.py
├── ...
├── build
│   └── dist
│         └── sample-lib-0.2.19.tar.gz
└── ...
```
