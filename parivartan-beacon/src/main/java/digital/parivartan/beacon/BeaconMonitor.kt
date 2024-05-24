package digital.parivartan.beacon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

import org.altbeacon.beacon.*

class BeaconMonitor(private val context: Context) {
    private var beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(context)
    var region: Region

    init {
        BeaconManager.setDebug(false)

        val parser = BeaconParser()
            .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.beaconParsers.add(parser)

        region = Region("advertalyst-beacons", Identifier.parse("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA5"), null, null)

    }


    fun connect(apiKey: String) {
        // TODO: Implement logic to handle authentication and retrieve JWT based on local auth_key
        Log.i(TAG, "TODO: Implement secure auth using passed key")

        // TODO: This should also act as a gate check for connectivity to Parivartan servers to accept incoming events
        // Send relevant exceptions which the client can handle
    }

    fun startScanning(targetActivityClass: Class<*>) {
        try {
            setupForegroundService(targetActivityClass)
        } catch (e: SecurityException) {
            Log.d(TAG, "Not setting up foreground service scanning until location permission granted by user")
            return
        }

        beaconManager.startMonitoring(region)
        beaconManager.startRangingBeacons(region)

        val regionViewModel = beaconManager.getRegionViewModel(region)
        regionViewModel.rangedBeacons.observeForever(BeaconHelper(context).centralRangingObserver)
    }

    fun stopScanning() {
        beaconManager.stopMonitoring(region)
        beaconManager.stopRangingBeacons(region)
    }

    private fun setupForegroundService(targetActivityClass: Class<*>) {
        val builder = Notification.Builder(context, AppInfoHelper.getAppName(context))
        builder.setSmallIcon(AppInfoHelper.getAppIcon(context))

        val intent = Intent(context, targetActivityClass)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "My Notification Channel Description"
        val notificationManager =  context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.id)
        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(context).enableForegroundServiceScanning(builder.build(), 456)
        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }


    companion object {
        const val TAG = "BeaconMonitor"
    }
}
