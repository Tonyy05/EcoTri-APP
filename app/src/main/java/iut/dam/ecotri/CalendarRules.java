package iut.dam.ecotri;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarRules {

    /**
     * Retourne la liste des collectes pour un jour donné.
     * @param cal un Calendar positionné sur le jour choisi
     */
    public static List<String> getCollectionsForDay(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // ATTENTION : en Java
        // SUNDAY=1, MONDAY=2, TUESDAY=3, ..., SATURDAY=7

        List<String> res = new ArrayList<>();

        // Déchets ménagers : lundi, mercredi, vendredi
        if (day == Calendar.MONDAY
                || day == Calendar.WEDNESDAY
                || day == Calendar.FRIDAY) {
            res.add("Déchets ménagers");
        }

        // Recyclables : mardi
        if (day == Calendar.TUESDAY) {
            res.add("Recyclables");
        }

        // Verre : jeudi
        if (day == Calendar.THURSDAY) {
            res.add("Verre");
        }

        // Végétaux : lundi
        if (day == Calendar.MONDAY) {
            res.add("Végétaux");
        }

        // Encombrants : mardi
        if (day == Calendar.TUESDAY) {
            res.add("Encombrants");
        }

        return res;
    }

    /**
     * Petit helper pour afficher un texte propre type :
     * "Aujourd'hui : Déchets ménagers, Végétaux"
     */
    public static String buildMessageForDay(Calendar cal) {
        List<String> list = getCollectionsForDay(cal);
        if (list.isEmpty()) {
            return "Aucune collecte aujourd'hui";
        }
        StringBuilder sb = new StringBuilder("Aujourd'hui : ");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
