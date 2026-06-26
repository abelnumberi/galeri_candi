package com.abelnumberi.galericandi.network

import com.abelnumberi.galericandi.database.Temple
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TempleApiService {
    @GET("temples")
    suspend fun getTemples(
        @Query("userId") userId: String
    ): List<Temple>

    @POST("temples")
    suspend fun createTemple(
        @Body temple: Temple
    ): Temple

    @PUT("temples/{id}")
    suspend fun updateTemple(
        @Path("id") id: String,
        @Body temple: Temple
    ): Temple

    @DELETE("temples/{id}")
    suspend fun deleteTemple(
        @Path("id") id: String
    ): Temple
}
