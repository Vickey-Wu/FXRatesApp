package com.example.fxratesapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fxratesapp.R
import com.example.fxratesapp.data.AppDatabase
import com.example.fxratesapp.data.RateRepository
import kotlin.math.roundToLong

class RateAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).alertDao()
        val alerts = dao.getEnabled()
        if (alerts.isEmpty()) return Result.success()

        val repo = RateRepository()
        val byBase = alerts.groupBy { it.base }

        for ((base, baseAlerts) in byBase) {
            val symbols = baseAlerts.map { it.target }.distinct()
            val latest = repo.getLatest(base, symbols)
            for (alert in baseAlerts) {
                val rate = latest.rates[alert.target] ?: continue
                val converted = alert.amount * rate
                val hit = if (alert.operator == ">=") {
                    converted >= alert.threshold
                } else {
                    converted <= alert.threshold
                }
                if (hit) {
                    notifyAlert(alert.base, alert.target, alert.amount, converted, alert.operator, alert.threshold)
                }
            }
        }

        return Result.success()
    }

    private fun notifyAlert(base: String, target: String, amount: Double, converted: Double, op: String, threshold: Double) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "rate_alerts"
        val channel = NotificationChannel(channelId, applicationContext.getString(R.string.rate_alerts_channel), NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)

        val text = String.format("%s %.2f -> %s %.4f %s %.4f", base, amount, target, converted, op, threshold)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("FX Rate Alert")
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}
