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
