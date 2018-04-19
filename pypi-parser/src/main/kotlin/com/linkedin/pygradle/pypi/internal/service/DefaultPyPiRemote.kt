package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.exception.RequestWasNotSuccessfulException
import com.linkedin.pygradle.pypi.exception.UnableToMakeHttpRequestException
import com.linkedin.pygradle.pypi.internal.http.PyPiResource
import com.linkedin.pygradle.pypi.service.PyPiPackageDetails
import com.linkedin.pygradle.pypi.service.PyPiRemote
import okhttp3.OkHttpClient
import java.util.*

internal class DefaultPyPiRemote(url: String, okHttpClient: OkHttpClient) : PyPiRemote {

    private val pypiResource: PyPiResource
    private val cachedResponses: MutableMap<String, PyPiPackageDetails> = Collections.synchronizedMap(mutableMapOf())

    init {
        val retrofit = buildRetrofit(url, okHttpClient)
        pypiResource = retrofit.create(PyPiResource::class.java)
    }

    override fun resolvePackage(name: String): PyPiPackageDetails {
        return cachedResponses.computeIfAbsent(name.toLowerCase()) {
            val response = pypiResource.getManifest(name).execute()
            if (!response.isSuccessful) {
                throw RequestWasNotSuccessfulException(name)
            }

            val packageDetailsBody = response.body() ?: throw RequestWasNotSuccessfulException(name)
            return@computeIfAbsent DefaultPyPiPackageDetails(packageDetailsBody)
        }
    }
}
