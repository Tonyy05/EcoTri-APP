package iut.dam.ecotri;

public class Decheterie {
    public int id;
    public String nom;
    public String adresse;
    public String horaires;
    public String acces;
    public double lat;
    public double lng;

    public Decheterie(int id, String nom, String adresse, String horaires, String acces,
                      double lat, double lng) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.horaires = horaires;
        this.acces = acces;
        this.lat = lat;
        this.lng = lng;
    }
}
