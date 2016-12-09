# Example Machine Learning Project (SVM on the IRIS dataset)

This example project will give you a starting point for applying and/or developing machine learning algorithms for the analysis of your data.
For the sake of simplicity, we will use the classical text-book example using an SVM from the scipy/numpy/sklearn stack on the IRIS dataset.

If not already done, please have a look at the example-project for the general pygradle project setup; this example assumes a working environment on your local computer.

Note that all the following commands assume that your PWD is this example project.

## One-time project setup

As for the example project, you will need a setup.py in order to build the project; therefore, generate it (once) by calling:

```
$ ./gradlew generateSetupPy
```

Note that the setup.py file already exists and has been customized in order to generate an executable (see below).
This required to define an entry_point for console_scripts, which now points to the main function which is responsible to train the classifier.

## Dependencies

We will be using the pivy-importer to import all dependencies of our local project from pypi into a local file system path.
Here, it is assumed that the pivy-importer is available on your local machine (it merely boils down to a ./gradlew assemble in that directory, and maybe copying the assembled jar to an appropriate place).
For convenience, I am using the shadowJar of the pivy-importer, though the other should work as well.

For the dependencies of this sample project, you can import those using

	java -jar ../../build/pivy-importer/libs/pivy-importer-0.3.36-SNAPSHOT-all.jar --repo /tmp/repo numpy:1.11.2 pandas:0.19.1 scipy:0.18.1 scikit-learn:0.18 python-dateutil:2.6.0 --replace numpy:1.6.1:numpy=1.11.2,scipy:0.9.0=scipy:0.18.1

and as a pre-requisite, make sure that development tools (python developement headers, etc.) are present such that the libraries above can be compiled.

## Building the project

You are now ready to run your build, using

```
$ ./gradlew build
```

Then, after a longish build run, you should be able to call the executable in order to see the output which should be similar to this:

	# ./build/deployable/bin/classify_iris
	Accuracy score on the IRIS dataset using a 60/40 split: 0.966666666667

Great! You just managed to use the scipy/numpy/sklearn stack with pygradle!
