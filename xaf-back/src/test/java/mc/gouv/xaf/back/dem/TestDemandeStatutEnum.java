package mc.gouv.xaf.back.dem;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum représentant les états possibles pour une demande
 * 
 * @author qdeme
 * 
 */
public enum TestDemandeStatutEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", false),
    EN_COURS_TRAIT("En cours de traitement", false),
    ACCEPTEE_SOUS_RESERVE("Acceptée sous réserve", true),
    REFUSEE("Refusée", true),
    EN_ATTENTE_COMPL("En attente d'informations complémentaires", true),
    EN_ATTENTE_FINALISATION("En attente de finalisation", false),
    VALIDEE("Validée", false),
    ANNULEE("Annulée", true);

    public String libelle;

    public boolean containsMotifs;

    TestDemandeStatutEnum(String libelle, boolean containsMotifs) {
        this.libelle = libelle;
        this.containsMotifs = containsMotifs;
    }

    @Override
    public String toString() {
        return libelle;
    }

    /**
     * Retourne la liste des statuts pour lesquels des motifs peuvent correspondre
     * 
     * @return
     */
    public List<TestDemandeStatutEnum> getCandidatesForMotifs() {
        List<TestDemandeStatutEnum> listEnumtoTrue = new ArrayList<TestDemandeStatutEnum>();
        for (TestDemandeStatutEnum obj : TestDemandeStatutEnum.values()) {
            if (obj.containsMotifs == true) {
                listEnumtoTrue.add(obj);
            }
        }
        return listEnumtoTrue;
    }

}
