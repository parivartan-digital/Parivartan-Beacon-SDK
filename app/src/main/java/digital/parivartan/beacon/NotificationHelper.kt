package digital.parivartan.beacon

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface EventsApi {
    @POST("/functions/v1/ble-events-collector-updated")
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


object NotificationHelper {
    private const val baseUrl = "https://bqktavtbwtpxooctiles.supabase.co"
    private const val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJxa3RhdnRid3RweG9vY3RpbGVzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTUxNDUyMDQsImV4cCI6MjAzMDcyMTIwNH0.Y9tu2sgiz4MBhOkEfdwtzCt--h31vFA8AyXinIphP8Y"

    private fun getAuthorizedClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(token))
            .build()
    }

    private fun getInstance(): Retrofit {
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
            Log.d("NotificationHelper", "Event send successfully")
        } else {
            // Request failed, handle error response
            val errorBody = response.errorBody()?.string()
            Log.e("NotificationHelper", "Error Body: $errorBody")
        }
    }
}
