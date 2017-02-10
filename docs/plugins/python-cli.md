# com.linkedin.python-cli

## About

The `com.linkedin.python-cli` extends the `com.linkedin.python-pex` plugin so that TAB completion scripts can be generated when projects use Click.

## Usage

```
plugins {
    id 'com.linkedin.python-cli', version <latest version>
}
```

TAB completion generation is disabled by default, to enable it do the following:

```
python.cli.generateCompletions = true
```
