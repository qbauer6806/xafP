package mc.gouv.xaf.backweb.dto;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Classe permettant de rajouter des champs dans DemandeDTO, qui sont utiles pour les démarches, sans avoir à encombrer
 * DEM avec ça (et avec le calcul de ces champs)
 *
 * @author qdeme
 */
@Setter
@Getter
public class AfBackDemandeDTO extends DemandeDTO {

    private String agentAffectePrenom;

    private String agentAffecteNom;

    public AfBackDemandeDTO(DemandeDTO demande) {
        setAgent(demande.getAgent());
        setCanal(demande.getCanal());
        setComplements(demande.getComplements());
        setContenu(demande.getContenu());
        setContenuTrad(demande.getContenuTrad());
        setCourrierDateReception(demande.getCourrierDateReception());
        setCourrierRefInterne(demande.getCourrierRefInterne());
        setCourriers(demande.getCourriers());
        setCreeParAgentId(demande.getCreeParAgentId());
        setData(demande.getData());
        setDateCreation(demande.getDateCreation());
        setDateDerModif(demande.getDateDerModif());
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

}
