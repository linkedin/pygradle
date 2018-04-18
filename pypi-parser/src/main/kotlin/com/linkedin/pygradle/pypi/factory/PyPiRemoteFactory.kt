package com.linkedin.pygradle.pypi.factory

import com.linkedin.pygradle.pypi.internal.service.DefaultPyPiRemote
import com.linkedin.pygradle.pypi.service.PyPiRemote
import okhttp3.OkHttpClient

/**
 * Builds a [PyPiRemote]
 */
object PyPiRemoteFactory {
    const val PYPI_URL = "https://pypi.org"

    /**
     * Default PyPi Repo
     */
    @JvmStatic
    fun buildPyPiRemote(): PyPiRemote = buildPyPiRemote(PYPI_URL)

    /**
     * Custom PyPi Repo, with default OkHttp
     */
    @JvmStatic
    fun buildPyPiRemote(remoteUrl: String): PyPiRemote {
        val okHttpClient = OkHttpClient.Builder().build()
        return buildPyPiRemote(remoteUrl, okHttpClient)
    }

    /**
     * Custom PyPi repo with custom OkHttp
     */
    @JvmStatic
    fun buildPyPiRemote(remoteUrl: String, client: OkHttpClient): PyPiRemote = DefaultPyPiRemote(remoteUrl, client)
}
