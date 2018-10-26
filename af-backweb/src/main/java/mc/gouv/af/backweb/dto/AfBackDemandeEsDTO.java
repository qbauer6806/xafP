package mc.gouv.af.backweb.dto;

import mc.gouv.af.back.data.es.model.DemandeEsDTO;
import mc.gouv.af.back.data.es.model.DemandeEsRechercheDTO;

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
        setComplements(demande.getComplements());
        setContenu(demande.getContenu());
        setCourrierDateReception(demande.getCourrierDateReception());
        setCourrierRefInterne(demande.getCourrierRefInterne());
        setCourriers(demande.getCourriers());
        setCreeParAgentId(demande.getCreeParAgentId());
        setData(demande.getData());
        setDateCreation(demande.getDateCreation());
        setDateDerModif(demande.getDateDerModif());
        setAccess(demande.getAccess());
        setDernierStatut(demande.getDernierStatut());
        // setFichiers(demande.getFichiers());
        setIdentifiant(demande.getIdentifiant());
        setLangue(demande.getLangue());
        setObservations(demande.getObservations());
        setPkDemandes(demande.getPkDemandes());
        setStatuts(demande.getStatuts());
    }

    public AfBackDemandeEsDTO(DemandeEsRechercheDTO demande) {
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
        setAccess(demande.getAccess());
        setDernierStatut(demande.getDernierStatut());
        // setFichiers(demande.getFichiers());
        setIdentifiant(demande.getIdentifiant());
        setLangue(demande.getLangue());
        setObservations(demande.getObservations());
        setPkDemandes(demande.getPkDemandes());
        setStatuts(demande.getStatuts());
        setHighlightedField(demande.getHighlightedField());
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
