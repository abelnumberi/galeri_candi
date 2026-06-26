package com.abelnumberi.galericandi.network

import com.squareup.moshi.Json
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class ImgBBResponse(
    @Json(name = "data")
    val data: ImgBBData?,
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "status")
    val status: Int
)

data class ImgBBData(
    @Json(name = "url")
    val url: String
)

interface ImgBBApiService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part
    ): ImgBBResponse
}
