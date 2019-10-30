package mc.gouv.xaf.shared.dto;

/**
 * Modélise un message d'erreur
 * 
 * @author qdeme
 *
 */
public class ErrorDTO {

    private Libelle[] errors;
    
    public ErrorDTO(Libelle[] errors) {
        this.errors = errors;
    }
    
    public static class Libelle {
        
        private String libelle;
        
        public Libelle(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }

        public void setLibelle(String libelle) {
            this.libelle = libelle;
        }
        
    }

    public Libelle[] getErrors() {
        return errors;
    }

    public void setErrors(Libelle[] errors) {
        this.errors = errors;
    }
    
}
