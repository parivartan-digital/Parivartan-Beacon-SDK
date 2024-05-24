package org.altbeacon.beaconreference

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

import digital.parivartan.beacon.BeaconMonitor

class BeaconReferenceApplication: Application() {
    lateinit var beaconMonitor: BeaconMonitor

    override fun onCreate() {
        super.onCreate()
        beaconMonitor = BeaconMonitor(this)
        beaconMonitor.connect("MY-API-KEY")
        beaconMonitor.startScanning(MainActivity::class.java)

    }

    // Define a NotificationHandler interface
    interface NotificationHandler {
        fun sendNotification(context: Context, title: String, message: String)
    }

    // Implement the interface in your SDK class
    class MySDK : NotificationHandler {
        // Implement the sendNotification method
        override fun sendNotification(context: Context, title: String, message: String) {
            val builder = NotificationCompat.Builder(context, "beacon-ref-notification-id")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background)

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntent(Intent(context, MainActivity::class.java))
            val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(resultPendingIntent)

            val channel =  NotificationChannel(
                "beacon-ref-notification-id",
                "My Notification Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "My Notification Channel Description"

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channel.id)
            notificationManager.notify(1, builder.build())
        }
    }



    companion object {
        val TAG = "BeaconReference"
    }

}