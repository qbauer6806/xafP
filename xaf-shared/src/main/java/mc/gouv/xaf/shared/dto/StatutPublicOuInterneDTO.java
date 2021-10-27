package mc.gouv.xaf.shared.dto;

/**
 * Contient le nom et le libellé d'un statut public (DEM) ou interne (démarche)
 *
 * @author qdeme
 */
public class StatutPublicOuInterneDTO {
    
    private String name;

    private String libelle;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
