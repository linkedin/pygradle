package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.factory.PyPiRemoteFactory
import com.linkedin.pygradle.pypi.model.DependencyOperator
import com.linkedin.pygradle.pypi.model.extra.DefaultDependencyCondition
import com.linkedin.pygradle.pypi.model.extra.SystemDependencyCondition
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Timeout

class DefaultDependencyCalculatorTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    @Timeout(20)
    def 'run import of flake8'() {
        def okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        def pyPiRemote = PyPiRemoteFactory.buildPyPiRemote(PyPiRemoteFactory.PYPI_URL, okHttpClient)
        def cacheDir = temporaryFolder.newFolder("cache-dir")
        def calculator = new DefaultDependencyCalculator(okHttpClient, pyPiRemote, cacheDir, { true })

        when:
        def packageDetails = pyPiRemote.resolvePackage("flake8")
        def dependencies = calculator.calculateDependencies(packageDetails, packageDetails.latestVersion)

        then:
        def expectedDependencies = [
            new ExpectedDependencies('mccabe', '0.6.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('mccabe', '0.7.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.0.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pycodestyle', '2.4.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.5.0', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('pyflakes', '1.7.0', DependencyOperator.LESS_THAN, [new DefaultDependencyCondition()] as Set),
            new ExpectedDependencies('configparser', '3.5.0', DependencyOperator.GREATER_THAN_EQUAL, [new SystemDependencyCondition("python", "3.2", DependencyOperator.LESS_THAN)] as Set),
            new ExpectedDependencies('enum34', '1.1.6', DependencyOperator.GREATER_THAN_EQUAL, [new SystemDependencyCondition("python", "3.4", DependencyOperator.LESS_THAN)] as Set),
        ]

        dependencies.dependencies.size() == expectedDependencies.size()

        println "All dependencies for -------------- ${dependencies.dependencies}"
        expectedDependencies.each { dep ->
            println "Searching for -------------- $dep"
            assert dependencies.dependencies.find { dep.isEqual(it) }
        }

        cacheDir.listFiles().size() == 1
    }

    @Timeout(20)
    def 'run import of Sphinx'() {
        def okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        def pyPiRemote = PyPiRemoteFactory.buildPyPiRemote(PyPiRemoteFactory.PYPI_URL, okHttpClient)
        def cacheDir = temporaryFolder.newFolder("cache-dir")
        def calculator = new DefaultDependencyCalculator(okHttpClient, pyPiRemote, cacheDir, { true })

        when:
        def packageDetails = pyPiRemote.resolvePackage("Sphinx")
        def dependencies = calculator.calculateDependencies(packageDetails, packageDetails.latestVersion)

        then:
        def expectedDependencies = [
            new ExpectedDependencies('Babel', '1.3', DependencyOperator.GREATER_THAN_EQUAL, [new DefaultDependencyCondition()] as Set),
        ]

        dependencies.dependencies.size() == 27

        println "All dependencies for -------------- ${dependencies.dependencies}"
        expectedDependencies.each { dep ->
            println "Searching for -------------- $dep"
            assert dependencies.dependencies.find { dep.isEqual(it) }
        }

        cacheDir.listFiles().size() == 1
    }
}
