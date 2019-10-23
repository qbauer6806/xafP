package mc.gouv.xaf.back.shared.dto;

/**
 * Modélise une erreur retournée au client
 * 
 * @author qdeme
 *
 */
public class MessageErreurDTO {

    private String nom;

    private String libelle;

    public MessageErreurDTO(String nom, String libelle) {
        this.nom = nom;
        this.libelle = libelle;
    }

    public String getNom() {
        return nom;
    }

    public String getLibelle() {
        return libelle;
    }

}
