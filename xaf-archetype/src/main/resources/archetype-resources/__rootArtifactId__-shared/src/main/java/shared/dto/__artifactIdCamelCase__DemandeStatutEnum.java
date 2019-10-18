#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum représentant les états possibles pour une demande
 * 
 * @author mpavone
 * 
 */
public enum ${artifactIdCamelCase}DemandeStatutEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", false),
    EN_COURS_TRAIT("En cours de traitement", false),
    REFUSEE("Refusée", true),
    EN_ATTENTE_COMPL("En attente d'informations complémentaires", true),
    ACCORDEE("Accordée", true),
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
