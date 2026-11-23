package iut.dam.ecotri.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class HistoryMessage(
    val role: String,   // "user" ou "assistant"
    val content: String
)

data class ChatRequest @JvmOverloads constructor(
    val conversationId: String,
    val message: String,
    val history: List<HistoryMessage> = emptyList()
)

data class ChatPrefill @JvmOverloads constructor(
    val typeBac: String? = null,
    val raison: String? = null,
    val commentaire: String? = null
)

data class ChatAction @JvmOverloads constructor(
    val type: String? = null,     // "OPEN_SCREEN"
    val screen: String? = null,   // "DEMANDE_BAC"
    val prefill: ChatPrefill? = null
)

data class ChatResponse @JvmOverloads constructor(
    val reply: String? = null,
    val action: ChatAction? = null
)

interface ChatApi {
    @POST("chat") // ✅ car BASE_URL finit déjà par /api/
    fun chat(
        @Header("Authorization") bearer: String,
        @Body body: ChatRequest
    ): Call<ChatResponse>
}