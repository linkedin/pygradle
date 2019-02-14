# Developers

## Building PyGradle

To build PyGradle run the following command.

    ./gradlew build

This will compile the project, run the tests and integration tests.

To publish to a local repository, run the following command.

    ./gradlew publishToMavenLocal

This will publish PyGradle artifacts to `~/.m2` using maven as the metadata
format. To use this version, be sure to update the version in the project under
test and add `mavenLocal()` to the repositories. This will configure your
project to look in `~/.m2` in addition to other repositories you have
configured when pulling artifacts.

If you are building on Windows, PyGradle will avoid using your system temporary
folder for integration tests and instead create and use the folder `c:\tmp`.  This
is to avoid issues with Windows's 260 character path limit.  Make sure your user
account has the ablity to create and use this folder or your integration tests will fail.

If you are using Windows 10, it is possible to go beyond that 260 character limit,
but not with this product.  That policy change only applies to managed applications.
PyGradle's unit tests are not managed applications.


## Contributing

To contribute to PyGradle please fork the project, make your changes locally,
and open a pull request. We'll ask that you add the following information and
adhere to the following conventions.

1. Include a summary and description of why this change is necessary.
   Describing not just _what_ but also _why_ provides context that will help
   reviewers review your change. Please cite relevant issues, if applicable.
2. Make sure your change is tested and passes code style and linting tools that
   we use, such as `checkstyle` and `codenarc`. Adding tests where there are
   not any is required.
3. Please squash your commits into a single commit before submitting a pull
   request. This keeps our change log clean.
4. Please be respectful to everyone involved with the project, including those
   that have opened issues. We do our best to serve everyone, but remember that
   if there is a problem or something missing for you, you're likely the best
   candidate to fix it.
5. Please be patient while we review your changes.

## Releasing

We choose to validate releases of PyGradle internally before releasing them
publicly. Therefore, if you want a release, please reach out to us so that we
can do this if you're blocked on getting a new release out. As with
contributions, please be patient while we do this.


## Debugging in IntelliJ

TL;DR: you'll need to do the following basically:
- Modify the build.gradle of a pygradle project you wish to debug on (we'll refer to this as `/path/to/myproject` below)
- Create a usable run/debug configuration in IntelliJ

Detailed instructions:

- `./gradlew build` (`./` refers to your local pygradle checkout, including in points below)
- Open your pygradle project in IntelliJ
    - Select **Use auto-import**, make sure the other 2 checkboxes are NOT checked
    - Select **Use default gradle wrapper** from the 3 radio options
    - Everything else default ("Gradle home" empty, project format .idea)
- Go to menu `File` -> `Project Structure`
    - Select `Libraries` (left pane), click the `+` button (middle pane) to add a `Java` library, browse to `./gradle/wrapper`, click `Open`
        - Choose module `pygradle-plugin` when prompted
    - Select `Modules` (left pane), then `pygradle-plugin` (middle pane), tab `Dependencies` (right pane)
        - Find `gradle-api 3.0` in the list, and select `Provided` (instead of `Compiled`, from the right-side combo)
        - Similarly find `wrapper` (which should be at the bottom of list), select `Runtime` for it
    - Click `OK` to save these changes
- Go to menu `Run` -> `Edit configurations`
    - Hit the `+` button, select `Application`, give it a name for example "myproject flake8"
    - Select `Single instance only` (top right corner of dialog, to avoid multiple gradle runs by mistake...)
    - **Main class**: `org.gradle.wrapper.GradleWrapperMain`
    - **VM options**: `-Dorg.gradle.daemon=false` (note: this is important, no daemon or you won't be able to actually use breakpoints)
    - **Program args**: for example `flake8`, or `clean build` etc
    - **Working dir**: `/path/to/myproject` (the pygradle project you wish to debug on)
    - **Env vars**: empty
    - **Use classpath of module**: `pygradle-plugin`
    - **JRE**: you can use the default
    - **Before launch**:
        - Remove the existing `Build` task
        - Hit the `+` button and choose `Run gradle task` type
        - **Gradle project**: select `pygradle` (using the first `...` button)
        - In **Tasks** field: enter `publishToMavenLocal`
        - Leave the other 2 fields empty, hit `OK`
    - Hit `OK`
- Run `./gradlew publishToMavenLocal`, note the version that it published, let's say version was `0.4.6-SNAPSHOT` (we'll use that below)
- Edit your `/path/to/myproject/build.gradle` file:
    - Comment out your previous reference to python plugin, for example: `// plugins { id "com.linkedin.python-cli" version "0.4.5" }`
    - Add this section at the top of the file (you can use any `com.linkedin.python*` plugin variant):
```
buildscript {
  repositories {
    mavenLocal()
    jcenter()
  }
  dependencies {
    classpath "com.linkedin.pygradle:pygradle-plugin:0.4.6-SNAPSHOT"
  }
}
apply plugin: "com.linkedin.python-cli"
```

- Save your file, and try running `/path/to/myproject/gradlew tasks`, verify that it still works
- Go back to IntelliJ, now you can set breakpoints: try putting one in class `PythonPlugin`, 1st line of `apply()` function
- Hit menu `Run` -> `Debug` on your previously created "myproject flake8" above, you should be good to go
