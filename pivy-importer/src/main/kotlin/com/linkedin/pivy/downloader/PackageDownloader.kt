package com.linkedin.pivy.downloader

import com.linkedin.pivy.ImporterOptions
import com.linkedin.pivy.NoVersionProvidedException
import com.linkedin.pygradle.pypi.factory.DependencyCalculatorFactory
import com.linkedin.pygradle.pypi.factory.PyPiRemoteFactory
import com.linkedin.pygradle.pypi.factory.PythonPackageVersionFactory
import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.model.PackageType
import com.linkedin.pygradle.pypi.model.PackageType.Companion.matchPackageType
import com.linkedin.pygradle.pypi.model.ParseReport
import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

internal class PackageDownloader(private val options: ImporterOptions,
                                 private val requiredVersionsContainer: RequiredVersionContainer,
                                 private val supportedTypes: List<String>) {

    private val log = LoggerFactory.getLogger(PackageDownloader::class.java)

    private val cacheDir = options.cache

    private val httpClient = OkHttpClient.Builder()
        .proxy(createProxy())
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(makeLoggingInterceptor())
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private fun makeLoggingInterceptor(): Interceptor {

        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message -> log.debug(message) })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        return httpLoggingInterceptor
    }

    private val pyPiRemote = PyPiRemoteFactory.buildPyPiRemote(PyPiRemoteFactory.PYPI_URL, httpClient)
    private val dependencyCalculator = DependencyCalculatorFactory.buildDependencyCalculator(
        httpClient, pyPiRemote, cacheDir, { it.matchPackageType() == PackageType.S_DIST })

    /**
     * Downloads a given package.
     */
    fun downloadPackage(name: String): ParseReport? {
        val resolvePackage = pyPiRemote.resolvePackage(name)
        val (version, availableReleased) = getVersion(name, resolvePackage)

        if (availableReleased == null) {
            log.warn("Unable to find a release of {} with version {}", name, version)
            return null
        }

        val packageRelease = availableReleased.firstOrNull { it.packageType in supportedTypes }
        if (packageRelease == null) {
            log.warn("Unable to find a compatible release (supported: {}) for {} at version {}", supportedTypes, name, version)
            return null
        }

        return dependencyCalculator.calculateDependencies(resolvePackage, PythonPackageVersionFactory.makeVersion(version))
    }

    private fun getVersion(name: String, resolvePackage: PyPiPackageDetails): Pair<String, List<PackageRelease>?> {
        var version = requiredVersionsContainer.findVersion(name)

        if (version == null && !options.requireAllVersions) {
            val pickedVersion = resolvePackage.getLatestVersion()!!.toVersionString()
            log.warn("Registering {} to use {} globally", name, pickedVersion)
            requiredVersionsContainer.register(name, pickedVersion)
        }

        version = version ?: requiredVersionsContainer.findVersion(name)
        version ?: throw NoVersionProvidedException("No version was provided for $name")

        val availableReleased = resolvePackage.getPackageInfo().releases[version]
        return Pair(version, availableReleased)
    }

    private fun createProxy(): Proxy? {
        val proxyPort = System.getProperty(HTTP_PROXY_PORT, null)?.toInt() ?: return null
        val proxyHost = System.getProperty(HTTP_PROXY_HOST, null) ?: return null

        log.debug("Detected {} = '{}' and {} = '{}' in the system properties.",
            HTTP_PROXY_HOST, proxyHost, HTTP_PROXY_PORT, proxyPort)
        return Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort))
    }

    private companion object {
        private const val HTTP_PROXY_HOST = "http.proxyHost"
        private const val HTTP_PROXY_PORT = "http.proxyPort"
    }
}
