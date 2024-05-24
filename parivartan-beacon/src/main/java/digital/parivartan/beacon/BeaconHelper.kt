package digital.parivartan.beacon

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Beacon
import java.util.concurrent.Executors

class BeaconHelper(context: Context) {

    private val beaconNotificationTimestamps = object : LinkedHashMap<Beacon, Long>(MAX_BEACONS, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Beacon, Long>?): Boolean {
            return size > MAX_BEACONS
        }
    }
    private val eventNotifier: EventNotifier = EventNotifier(context)

    private fun sendEventNotification(beacon: Beacon) {
        val executor = Executors.newSingleThreadExecutor()
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        executor.execute {
            /*
            // TODO: Should we proceed if the adInfo is null? Does it add value
            if(adInfo == "") {
                Log.i(TAG, "Could not extract advertising ID. Not proceeding")
                return@execute
            }
            */
            coroutineScope.launch {
                eventNotifier.sendEventNotification(beacon)
            }
        }
    }

    val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val currentTimeMillis = System.currentTimeMillis()
        val rangeAgeMillis = currentTimeMillis - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)
        if (rangeAgeMillis < 10000) {
            Log.d(BeaconMonitor.TAG, "Ranged: ${beacons.count()} beacons")
            for (beacon: Beacon in beacons) {
                val lastNotificationTime = beaconNotificationTimestamps[beacon] ?: 0
                val timeSinceLastNotification = currentTimeMillis - lastNotificationTime

                Log.d(BeaconMonitor.TAG, "$beacon about ${beacon.distance} meters away")
                if (timeSinceLastNotification >= EXPIRATION_TIME) {
                    sendEventNotification(beacon)
                    // Update the last notification timestamp for this beacon
                    beaconNotificationTimestamps[beacon] = currentTimeMillis
                }
                else
                    Log.d(BeaconMonitor.TAG, "Not sending notification for already discovered beacon")
            }
        }
        else {
            Log.d(BeaconMonitor.TAG, "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
        }
    }


    companion object {
//        const val TAG = "BeaconHelper"
        const val MAX_BEACONS = 20
        const val EXPIRATION_TIME = 15 * 60 * 1000 // 15 minutes in milliseconds
//      const val EXPIRATION_TIME = 5 * 1000 // 5 seconds in milliseconds
    }
}
