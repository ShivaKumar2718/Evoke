package com.siva.evoke.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.siva.evoke.receivers.ChargerPlugInReceiver


class ChargerPluginService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "PluginServiceChannel"
    private lateinit var chargerPluginReceiver: ChargerPlugInReceiver

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .build()

        startForeground(NOTIFICATION_ID, notification)

        chargerPluginReceiver = ChargerPlugInReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(chargerPluginReceiver,intentFilter)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "CHARGER CONNECTION STATUS",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}