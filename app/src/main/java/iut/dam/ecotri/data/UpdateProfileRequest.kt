package iut.dam.ecotri.data

data class UpdateProfileRequest(
    val email: String,        // email actuel (pour retrouver l'utilisateur)
    val firstName: String,
    val lastName: String,
    val newEmail: String,
    val adresse: String?,
    val phone: String?,
    val dateNaissance: String?
)
