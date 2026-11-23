package iut.dam.ecotri.data

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val dateNaissance: String,
    val adresse: String,
    val phone: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dateNaissance: String,   // peut être null (côté API), mais tu gères ça
    val adresse: String,         // peut être null
    val phone: String,           // peut être null
    val role: String,
    val notificationsEmail: Boolean   // 🔹 NOUVEAU
)

data class LoginResponse(
    val token: String,
    val mustChangePassword: Boolean = false, // ✅
    val user: UserDto
)
