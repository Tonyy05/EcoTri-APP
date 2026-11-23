package iut.dam.ecotri.data

data class ChangePasswordRequest(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)
