package iut.dam.ecotri.data

data class DemandeBacResponse(
    val id: Long,
    val typeBac: String,
    val raison: String,
    val adresse: String,
    val commentaire: String?,
    var statut: String,
    val createdAt: String?,
    val updatedAt: String?
)
