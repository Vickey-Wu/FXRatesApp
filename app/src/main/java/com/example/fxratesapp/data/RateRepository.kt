package com.example.fxratesapp.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RateRepository {
    private val api: ApiService

    init {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder().addInterceptor(logger).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frankfurter.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    suspend fun getLatest(base: String, symbols: List<String>): LatestResponse {
        return api.latest(base = base, symbols = symbols.joinToString(","))
    }

    suspend fun getTimeSeries(base: String, symbols: List<String>, start: LocalDate, end: LocalDate): TimeSeriesResponse {
        val fmt = DateTimeFormatter.ISO_DATE
        val range = "${start.format(fmt)}..${end.format(fmt)}"
        return api.timeseries(range = range, base = base, symbols = symbols.joinToString(","))
    }
}
