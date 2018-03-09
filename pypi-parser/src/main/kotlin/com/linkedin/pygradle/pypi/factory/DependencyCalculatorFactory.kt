package com.linkedin.pygradle.pypi.factory

import com.linkedin.pygradle.pypi.internal.http.PackageRelease
import com.linkedin.pygradle.pypi.internal.service.DefaultDependencyCalculator
import com.linkedin.pygradle.pypi.service.DependencyCalculator
import com.linkedin.pygradle.pypi.service.PyPiRemote
import okhttp3.OkHttpClient
import java.io.File

object DependencyCalculatorFactory {
    @JvmStatic
    fun buildDependencyCalculator(okHttpClient: OkHttpClient, pyPiRemote: PyPiRemote, cacheFolder: File): DependencyCalculator {
        return buildDependencyCalculator(okHttpClient, pyPiRemote, cacheFolder, { true })
    }

    @JvmStatic
    fun buildDependencyCalculator(okHttpClient: OkHttpClient,
                                  pyPiRemote: PyPiRemote,
                                  cacheFolder: File,
                                  typeFilter: (PackageRelease) -> Boolean): DependencyCalculator {
        return DefaultDependencyCalculator(okHttpClient, pyPiRemote, cacheFolder, typeFilter)
    }
}
