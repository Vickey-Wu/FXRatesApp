package com.example.fxratesapp.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("v1/latest")
    suspend fun latest(
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): LatestResponse

    @GET("v1/{range}")
    suspend fun timeseries(
        @Path("range") range: String,
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): TimeSeriesResponse
}
