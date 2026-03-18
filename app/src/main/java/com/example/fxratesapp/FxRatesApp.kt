package com.example.fxratesapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fxratesapp.worker.RateAlertWorker
import java.util.concurrent.TimeUnit

class FxRatesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<RateAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "rate_alerts",
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }
}
