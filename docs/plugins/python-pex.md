# com.linkedin.python-pex

## About

The `com.linkedin.python-pex` plugin creates a [pex](https://pex.readthedocs.io/en/stable/) file as an artifact of the build. This pex comes in two flavors. The first is a 'fat pex' meaning that there will be a pex built for every console script in the project. In contrast, there are 'thin pex' files, where there is a single pex file created, but a script per application will be generated pointing at the pex file. By default, pygradle will generate thin pex applications. The `com.linkedin.python-pex` updates the `python` closure to include a `pex` option.
 
 Here are the options provided by the added `pex` option:
 ```
 python {
    pex {
        fatPex = false // default
        pexCache = project.file("$buildDir/pex-cache") // default
    }
 }
 ```
 
 The `com.linkedin.python-pex` plugin also applies [`com.linkedin.python`](./python.md).

## Usage

### Standard Usage

```
plugins {
    id 'com.linkedin.python-pex', version <latest version>
}
```

### Fat Pex Usage
```
plugins {
    id 'com.linkedin.python-pex', version <latest version>
}

python.pex.fatPex = true
```
