package com.linkedin.pygradle.pypi.internal.service

import com.linkedin.pygradle.pypi.internal.ObjectMapperContainer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

internal object RetrofitUtil {
    internal fun buildRetrofit(url: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(JacksonConverterFactory.create(ObjectMapperContainer.objectMapper))
            .client(okHttpClient)
            .build()
    }
}
