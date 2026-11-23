package iut.dam.ecotri.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    // Client HTTP avec logs
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // ✅ Instance Retrofit unique, réutilisée pour toutes les API
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Auth API
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    // Calendar API
    val calendarApi: CalendarApi by lazy {
        retrofit.create(CalendarApi::class.java)
    }

    val demandeBacApi: DemandeBacApi = retrofit.create(DemandeBacApi::class.java)

    // Chat API
    val chatApi: ChatApi by lazy {
        retrofit.create(ChatApi::class.java)
    }
}
