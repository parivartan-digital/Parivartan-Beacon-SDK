package digital.parivartan.beacon

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient

data class AdvertisingIdInfo(
    val aaid: String,
    val isLimitAdTrackingEnabled: Boolean
)

object AdIdHelper {
    fun getAdId(context: Context): AdvertisingIdInfo {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            Log.i("AdIdHelper", "$adInfo")
            AdvertisingIdInfo(
                aaid = adInfo.id ?: "",
                isLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled
            )
        } catch (e: Exception) {
            Log.i("AdIdHelper", "Unable to get an advertising ID. ${e.javaClass} ${e.message}")
            AdvertisingIdInfo(aaid = "", isLimitAdTrackingEnabled = true)
        }
    }
}

