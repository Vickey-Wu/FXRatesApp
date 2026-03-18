package com.example.fxratesapp.data

data class LatestResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

data class TimeSeriesResponse(
    val base: String,
    val start_date: String,
    val end_date: String,
    val rates: Map<String, Map<String, Double>>
)
