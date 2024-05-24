
package digital.parivartan.beacon

import android.content.Context
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import android.util.Log
import org.altbeacon.beacon.Beacon

data class EventPayload (
    val beaconUUID: String,
    val beaconMajorId: Int,
    val beaconMinorId: Int,
    val distance: Double,
    val timestamp: String,
    val idfa: String,
    val isLimitAdTrackingEnabled: Boolean
)

data class EventResponse (
    val success: Boolean,
    val message: String
)

class EventNotifier(private val context: Context) {
    suspend fun sendEventNotification(beacon: Beacon) {
        val adInfo = AdIdHelper.getAdId(context)
        Log.d("EventNotifier", "Advertising ID : $adInfo")
        val currentMoment: Instant = Clock.System.now()
        val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        val eventPayload = EventPayload(
            beacon.id1.toString(),
            beacon.id2.toInt(),
            beacon.id3.toInt(),
            beacon.distance,
            datetimeInSystemZone.toString(),
            adInfo.aaid,
            adInfo.isLimitAdTrackingEnabled,
        )
        Log.i("EventNotifier", eventPayload.toString())
        NotificationHelper.sendEvent(eventPayload)
//        apiService.sendEvent(event)
    }

}
