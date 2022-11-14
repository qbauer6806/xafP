package mc.gouv.xaf.shared.dto.export.archivage;

public class ArchivageFichierDeposeDTO {

    private String rang;

    private String nom;

    private String nomTiff;

    private String statut;

    private String date;

    public String getRang() {
        return rang;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomTiff() {
        return nomTiff;
    }

    public void setNomTiff(String nomTiff) {
        this.nomTiff = nomTiff;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
