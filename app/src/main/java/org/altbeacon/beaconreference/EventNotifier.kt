package org.altbeacon.beaconreference

import android.content.Context;
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.lang.Exception
import java.sql.Timestamp
import android.util.Log
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Region
import retrofit2.http.Body

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

interface EventsApi {
    @POST("/v2/events")
    suspend fun sendEvent(@Body eventPayload: EventPayload): Response<EventResponse>
}


object RetrofitHelper {

    val baseUrl = "https://4214-2401-4900-1f29-573c-43c-833e-936d-7c24.ngrok-free.app"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }

    suspend fun sendEvent(eventPayload: EventPayload) {
        val retrofit = getInstance()
        val eventsApi = retrofit.create(EventsApi::class.java)
        val response = eventsApi.sendEvent(eventPayload)
        // handle the response here
    }
}

class EventNotifier() {

    // Made public to allow retrieving availability
    fun getAdId(context: Context): Info? {
        return try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            adInfo
        } catch (e: Exception) {
            Log.i(TAG, "Unable to get an advertising ID. ${e.javaClass.toString()} ${e.message}")
            null
        }
    }



    suspend fun sendEventNotification(adInfo: Info, beacon: Beacon) {
        Log.i(TAG, "Advertising ID : ${adInfo.id}")
        val currentMoment: Instant = Clock.System.now()
        val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        var idfa = adInfo.id
        if (idfa == null) {
            idfa = "xxx"
        }
        val eventPayload = EventPayload(
            beacon.id1.toString(),
            beacon.id2.toInt(),
            beacon.id3.toInt(),
            beacon.distance,
            datetimeInSystemZone.toString(),
            idfa,
            adInfo.isLimitAdTrackingEnabled
        )
        Log.i(TAG, eventPayload.toString())
        RetrofitHelper.sendEvent(eventPayload)
//        apiService.sendEvent(event)
    }

    companion object {
        const val TAG = "EventNotifier"
    }
}
