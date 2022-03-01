package mc.gouv.xaf.backweb.dto;

import mc.gouv.xaf.back.data.es.model.DemandeEsDTO;
import mc.gouv.xaf.back.data.es.model.DemandeEsRechercheDTO;

/**
 * Classe permettant de rajouter des champs dans DemandeDTO, qui sont utiles pour les démarches, sans avoir à encombrer
 * DEM avec ça (et avec le calcul de ces champs)
 * 
 * @author qdeme
 *
 */
public class AfBackDemandeEsDTO extends DemandeEsRechercheDTO {

    private String agentAffectePrenom;

    private String agentAffecteNom;

    public AfBackDemandeEsDTO(DemandeEsDTO demande) {
        setCanal(demande.getCanal());
        setContenu(demande.getContenu());
        setCourrierDateReception(demande.getCourrierDateReception());
        setCourrierRefInterne(demande.getCourrierRefInterne());
        setNomsCourriers(demande.getNomsCourriers());
        // TODO setData(demande.getData());
        setDateCreation(demande.getDateCreation());
        setDateDerModif(demande.getDateDerModif());
        setDateDemande(demande.getDateDemande());
        setAccess(demande.getAccess());
        setDernierStatut(demande.getDernierStatut());
        setIdentifiant(demande.getIdentifiant());
        setLangue(demande.getLangue());
        setObservations(demande.getObservations());
        setPkDemandes(demande.getPkDemandes());
        // TODO setStatuts(demande.getStatuts());
        setAgentAffecteNomAffichage(demande.getAgentAffecteNomAffichage());
    }

    public AfBackDemandeEsDTO(DemandeEsRechercheDTO demande) {
        setCanal(demande.getCanal());
        setContenu(demande.getContenu());
        setCourrierDateReception(demande.getCourrierDateReception());
        setCourrierRefInterne(demande.getCourrierRefInterne());
        setNomsCourriers(demande.getNomsCourriers());
        // TODO setData(demande.getData());
        setDateCreation(demande.getDateCreation());
        setDateDerModif(demande.getDateDerModif());
        setDateDemande(demande.getDateDemande());
        setAccess(demande.getAccess());
        setDernierStatut(demande.getDernierStatut());
        setIdentifiant(demande.getIdentifiant());
        setLangue(demande.getLangue());
        setObservations(demande.getObservations());
        setPkDemandes(demande.getPkDemandes());
        // TODO setStatuts(demande.getStatuts());
        setHighlightedField(demande.getHighlightedField());
        setAgentAffecteNomAffichage(demande.getAgentAffecteNomAffichage());
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
