package com.linkedin.pygradle.pypi.internal.http

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PyPiResource {
    @GET("/pypi/{library}/json")
    fun getManifest(@Path("library") library: String): Call<PackageDetails>
}
