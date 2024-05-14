
package org.altbeacon.beaconreference

import android.content.Context;
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.lang.Exception
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.altbeacon.beacon.Beacon
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
    @POST("/functions/v1/ble-events-collector")
    suspend fun sendEvent(@Body eventPayload: EventPayload): Response<EventResponse>
}

class AuthorizationInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}


object RetrofitHelper {

    // val baseUrl = "https://f239-2401-4900-1cc5-5ff7-3801-86bb-2713-8d41.ngrok-free.app"
    //private const val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0"

    val baseUrl = "https://bqktavtbwtpxooctiles.supabase.co"
    private const val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJxa3RhdnRid3RweG9vY3RpbGVzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTUxNDUyMDQsImV4cCI6MjAzMDcyMTIwNH0.Y9tu2sgiz4MBhOkEfdwtzCt--h31vFA8AyXinIphP8Y"

    private fun getAuthorizedClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(token))
            .build()
    }


    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getAuthorizedClient())
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

        if (response.isSuccessful) {
            Log.d("Success", "Event send successfully")
        } else {
            // Request failed, handle error response
            val errorBody = response.errorBody()?.string()
            Log.e("Error", "Error Body: $errorBody")
        }
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