package iut.dam.ecotri.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DemandeBacApi {

    // POST http://10.0.2.2:3000/api/demandes-bac
    @POST("demandes-bac")
    fun createDemande(
        @Header("Authorization") auth: String,
        @Body body: DemandeBacRequest
    ): Call<Void>

    // GET http://10.0.2.2:3000/api/demandes-bac
    // (MAIRIE uniquement)
    @GET("demandes-bac")
    fun getDemandes(
        @Header("Authorization") auth: String
    ): Call<List<DemandeBacResponse>>

    // PATCH http://10.0.2.2:3000/api/demandes-bac/{id}
    // (MAIRIE uniquement)
    @PATCH("demandes-bac/{id}")
    fun updateDemandeStatut(
        @Header("Authorization") auth: String,
        @Path("id") id: Long,
        @Body body: UpdateStatutRequest
    ): Call<DemandeBacResponse>
}
