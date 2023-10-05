package mc.gouv.xaf.backweb.dto;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Classe permettant de rajouter des champs dans DemandeDTO, qui sont utiles pour les démarches, sans avoir à encombrer
 * DEM avec ça (et avec le calcul de ces champs)
 * 
 * @author qdeme
 *
 */
public class AfBackDemandeDTO extends DemandeDTO {

    private String agentAffectePrenom;

    private String agentAffecteNom;

    public AfBackDemandeDTO(DemandeDTO demande) {
        setAgentAffecteId(demande.getAgentAffecteId());
        setCanal(demande.getCanal());
        setComplements(demande.getComplements());
        setContenu(demande.getContenu());
        setCourrierDateReception(demande.getCourrierDateReception());
        setCourrierRefInterne(demande.getCourrierRefInterne());
        setCourriers(demande.getCourriers());
        setCreeParAgentId(demande.getCreeParAgentId());
        setData(demande.getData());
        setDateCreation(demande.getDateCreation());
        setDateDerModif(demande.getDateDerModif());
        setDemarcheId(demande.getDemarcheId());
        setDernierStatut(demande.getDernierStatut());
        setFichiers(demande.getFichiers());
        setFkAccess(demande.getFkAccess());
        setIdentifiant(demande.getIdentifiant());
        setLangue(demande.getLangue());
        setObservations(demande.getObservations());
        setPkDemandes(demande.getPkDemandes());
        setStatuts(demande.getStatuts());
        setUsagerId(demande.getUsagerId());
        setModificationTimestamp(demande.getModificationTimestamp());
        setContenuInitial(demande.getContenuInitial());
    }

    public String getAgentAffectePrenom() {
        return agentAffectePrenom;
    }

    public void setAgentAffectePrenom(String agentAffectePrenom) {
        this.agentAffectePrenom = agentAffectePrenom;
    }

    public String getAgentAffecteNom() {
        return agentAffecteNom;
    }

    public void setAgentAffecteNom(String agentAffecteNom) {
        this.agentAffecteNom = agentAffecteNom;
    }

}
