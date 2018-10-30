# Compatibility

## Linux

There aren't any known compatibility issues on Linux.

## OS X

There aren't any known compatibility issues on OS X.

## Windows

PyGradle has basic support for Windows. We would like to note that pex is not
offically supported on Windows, while it seems to work properly we cannot
guarantee it.

We're eager to review pull requests from our Windows user base!

## Python

pygradle currently supports Python 2.7, and 3.5 - 3.7.

### Caveats

- The integration tests require that Python be available in your PATH.
- The pex format defaults to "fat" - meaning each pex contains all dependencies..
- All pex files are renamed to include a `.py` extension, whereas on Unix based systems they are `.pex`.
- You must have Python installed and the `.py` extension needs to be registered to the Python interpreter you want to run.
