#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

/**
 * Référence les statuts internes du BPM (en fait, les tâches du BPM qui correspondent à un état
 * interne)
 * 
 * @author qdeme
 *
 */
public enum ${artifactIdCamelCase}StatutInterneEnum {
    
    validationComptableTask("En attente de validation comptable"),
    validationCGDTask("Envoyée au CGD");

    public String libelle;
    
    ${artifactIdCamelCase}StatutInterneEnum(String libelle) {
        this.libelle = libelle;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
    
}
