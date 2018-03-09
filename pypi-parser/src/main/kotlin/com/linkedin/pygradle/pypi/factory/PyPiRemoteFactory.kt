package com.linkedin.pygradle.pypi.factory

import com.linkedin.pygradle.pypi.internal.service.DefaultPyPiRemote
import com.linkedin.pygradle.pypi.service.PyPiRemote
import okhttp3.OkHttpClient

object PyPiRemoteFactory {
    val PYPI_URL = "https://pypi.python.org"

    @JvmStatic
    fun buildPyPiRemote(): PyPiRemote = buildPyPiRemote(PYPI_URL)

    @JvmStatic
    fun buildPyPiRemote(remoteUrl: String): PyPiRemote {
        val okHttpClient = OkHttpClient.Builder().build()
        return buildPyPiRemote(remoteUrl, okHttpClient)
    }

    @JvmStatic
    fun buildPyPiRemote(remoteUrl: String, client: OkHttpClient): PyPiRemote = DefaultPyPiRemote(remoteUrl, client)
}
