#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum représentant les états possibles pour une demande
 * 
 * @author qdeme
 * 
 */
public enum ${artifactIdCamelCase}DemandeStatutEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", false),
    EN_COURS_TRAIT("En cours de traitement", false),
    REFUSEE("Refusée", true),
    EN_ATTENTE_COMPL("En attente d'informations complémentaires", true),
    VALIDEE("Validée", true),
    VALIDEE_EN_ATTENTE_PAIEMENT("Validée en attente de paiement", false),
    VALIDEE_ET_PAYEE("Validée et payée", false),
    ANNULEE("Annulée", true);

    public String libelle;

    public boolean containsMotifs;

    ${artifactIdCamelCase}DemandeStatutEnum(String libelle, boolean containsMotifs) {
        this.libelle = libelle;
        this.containsMotifs = containsMotifs;
    }
    
    public static Map<String, String> getMap() {
        Map<String, String> statuts = new HashMap<>();
        for (${artifactIdCamelCase}DemandeStatutEnum statut : values()) {
            statuts.put(statut.name(), statut.libelle);
        }
        return statuts;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
