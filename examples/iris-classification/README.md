# Example Machine Learning Project (SVM on the IRIS dataset)

This example project shall give you a starting point for applying and/or developing Machine Learning algorithms for the analysis of your data.
For the sake of simplicity, we will use the classical text-book example using an SVM on the IRIS dataset, by using the scipy/numpy/sklearn stack.

If not already done, please have a look at the example-project for the general pygradle project setup; this example assumes a working environment on your local computer.

Note that all the following commands assume that your PWD is this example project.

## One-time project setup

As for the example project, you will need a setup.py in order to build the project; therefore, generate it (once) by calling:

```
$ ./gradlew generateSetupPy
```

For the following, this step is not needed to be executed.

## Dependencies

Note that we are using additional libraries which we need to manage in a local repository, since the pyGradlePyPi() repository is not a full mirror of pypi.
Therefore, we need to use the pivy-importer to import all dependencies from pypi into a local file system.
The overall strategy is to locally cache the PyPi repository (using Ivy-metadata) using the pivy-importer, and declare a repository dependency on the repository using a local file system path.

Here, it is assumed that the pivy-importer is available on your local machine (it merely boils down to a ./gradlew assemble in that directory, and maybe copying the assembled jar to an appropriate place).
For convenience, I am using the shadowJar of the pivy-importer, though the other should work as well.

For the dependencies of this sample project, you can import those using

	java -jar -Dhttp.proxyHost=172.16.101.231 -Dhttp.proxyPort=3128 ../../build/pivy-importer/libs/pivy-importer-0.3.36-SNAPSHOT-all.jar --repo /tmp/repo numpy:1.11.2 pandas:0.19.1 scipy:0.18.1 scikit-learn:0.18 python-dateutil:2.6.0 --replace numpy:1.6.1:numpy=1.11.2,scipy:0.9.0=scipy:0.18.1

and as a pre-requisite, make sure that development tools (python developement headers, etc.) are present such that the libraries above can be compiled.
Note that the environment variables for the proxy definition might either not be needed in your environment, or are not working until the PR 74 is merged in.

## Building the project

You are now ready to run your build, using

```
$ ./gradlew build
```

Then, after a longish build run, you should be able to call the executable in order to see the output which should be similar to this:

	root@64e86ed8927a:/tmp/working/pygradle_busche/examples/iris-classification# ./build/deployable/bin/classify_iris
	Accuracy score on the IRIS dataset using a 60/40 split: 0.966666666667
	root@64e86ed8927a:/tmp/working/pygradle_busche/examples/iris-classification#

Great! You just managed to use the scipy/numpy/sklearn stack with pygradle!
