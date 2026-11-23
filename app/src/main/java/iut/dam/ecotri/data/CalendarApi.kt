package iut.dam.ecotri.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CalendarApi {

    // Calendrier complet pour une ville (pour l'appli "user" classique)
    @GET("calendar")
    fun getCalendar(
        @Query("ville") ville: String = "Saint-Cloud"
    ): Call<List<CalendarEntry>>

    // ⚙️ Récupérer toutes les collectes d'UN JOUR pour une ville
    @GET("calendar/day")
    fun getDay(
        @Query("ville") ville: String = "Saint-Cloud",
        @Query("date") date: String          // "yyyy-MM-dd"
    ): Call<DayCalendarResponse>

    // ⚙️ Définir la liste complète des collectes d'UN JOUR (MAIRIE uniquement)
    @POST("calendar/set-day")
    fun setDay(
        @Header("Authorization") authHeader: String,  // "Bearer <token>"
        @Body body: SetDayRequest
    ): Call<DayCalendarResponse>
}
