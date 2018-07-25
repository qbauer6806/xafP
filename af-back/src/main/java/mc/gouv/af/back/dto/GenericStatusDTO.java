package mc.gouv.af.back.dto;

/**
 * 
 * Classe générique représentant un statut d'un {demarcheId}DemandeStatutEnum
 * 
 * @author qdeme
 *
 */
public class GenericStatusDTO {
    
    public String name;
    
    public String libelle;
    
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

}
