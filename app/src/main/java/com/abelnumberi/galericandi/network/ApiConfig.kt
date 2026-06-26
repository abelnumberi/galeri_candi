package com.abelnumberi.galericandi.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private val TEMPLE_BASE_URL = com.abelnumberi.galericandi.BuildConfig.MOCK_API_BASE_URL
    private const val IMGBB_BASE_URL = "https://api.imgbb.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val templeApiService: TempleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TEMPLE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TempleApiService::class.java)
    }

    val imgBBApiService: ImgBBApiService by lazy {
        Retrofit.Builder()
            .baseUrl(IMGBB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ImgBBApiService::class.java)
    }
}
