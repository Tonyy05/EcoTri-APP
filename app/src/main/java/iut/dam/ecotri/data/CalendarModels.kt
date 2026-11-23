package iut.dam.ecotri.data

/**
 * Une entrée brute de la table Calendar (utile si tu veux afficher
 * toute l’année côté utilisateur, mais pas indispensable pour l’admin).
 */
data class CalendarEntry(
    val id: Int,
    val ville: String,
    val dateCollecte: String,   // "yyyy-MM-dd"
    val typeBac: String         // "MENAGERES", "RECYCLABLES", etc.
)

/**
 * Réponse utilisée par :
 *  - GET /api/calendar/day
 *  - POST /api/calendar/set-day
 *
 * ⚠️ IMPORTANT : le nom du champ "types" doit matcher ce que tu utilises
 * en Java: day.getTypes()
 */
data class DayCalendarResponse(
    val date: String,           // "yyyy-MM-dd"
    val types: List<String>     // ex: ["MENAGERES", "VERRE"]
)

/**
 * Requête envoyée par l’admin (mairie) pour définir TOUTES les collectes d’un jour.
 * On envoie:
 *  - date : "yyyy-MM-dd"
 *  - types : liste de codes pour ce jour
 */
data class SetDayRequest(
    val date: String,
    val types: List<String>
)
