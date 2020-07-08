package mc.gouv.xaf.shared.dto;

import java.util.Date;
import java.util.List;

/**
 * DTO interne (non partagé) servant à DEM pour regrouper des critères de recherche de demandes
 *
 * @author qdeme
 *
 */
public class DemandeRechercheDTO {

    private String demarcheId;

    private String texte;

    private List<String> statuts;

    private List<DemandeCanalEnum> canaux;

    private String agentAffecteId;

    private Integer usagerId;

    private Date creationStartDate;

    private Date creationEndDate;

    // Pour le moment on gère la recherhe pour une data
    private DataRechercheDTO data;

    private String identifiant;

    private String[] searchFields;

    private boolean aucunCanal;

    private boolean aucunStatut;

    public DemandeRechercheDTO() {
        super();
    }

    public DemandeRechercheDTO(String demarcheId, String texte, List<String> statuts, List<DemandeCanalEnum> canaux,
                               String agentAffecteId, Integer usagerId, Date creationStartDate, Date creationEndDate,
                               DataRechercheDTO data, String identifiant) {
        super();
        this.demarcheId = demarcheId;
        this.texte = texte;
        this.statuts = statuts;
        this.canaux = canaux;
        this.agentAffecteId = agentAffecteId;
        this.usagerId = usagerId;
        this.creationStartDate = creationStartDate;
        this.creationEndDate = creationEndDate;
        this.data = data;
        this.identifiant = identifiant;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public List<String> getStatuts() {
        return statuts;
    }

    public void setStatuts(List<String> statuts) {
        this.statuts = statuts;
    }

    public String getAgentAffecteId() {
        return agentAffecteId;
    }

    public void setAgentAffecteId(String agentAffecteId) {
        this.agentAffecteId = agentAffecteId;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public Date getCreationStartDate() {
        return creationStartDate;
    }

    public void setCreationStartDate(Date creationStartDate) {
        this.creationStartDate = creationStartDate;
    }

    public Date getCreationEndDate() {
        return creationEndDate;
    }

    public void setCreationEndDate(Date creationEndDate) {
        this.creationEndDate = creationEndDate;
    }

    public List<DemandeCanalEnum> getCanaux() {
        return canaux;
    }

    public void setCanaux(List<DemandeCanalEnum> canaux) {
        this.canaux = canaux;
    }

    public DataRechercheDTO getData() {
        return data;
    }

    public void setData(DataRechercheDTO data) {
        this.data = data;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String[] getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(String[] searchFields) {
        this.searchFields = searchFields;
    }

    public boolean getAucunCanal() {
        return aucunCanal;
    }

    public void setAucunCanal(boolean aucunCanal) {
        this.aucunCanal = aucunCanal;
    }

    public boolean getAucunStatut() {
        return aucunStatut;
    }

    public void setAucunStatut(boolean aucunStatut) {
        this.aucunStatut = aucunStatut;
    }

    @Override
    public String toString() {
        return "DemandeRechercheDTO [demarcheId=" + demarcheId + ", texte=" + texte + ", statuts=" + statuts
                + ", canaux=" + canaux + ", agentAffecteId=" + agentAffecteId + ", usagerId=" + usagerId
                + ", creationStartDate=" + creationStartDate + ", creationEndDate=" + creationEndDate + ", data=" + data
                + ", identifiant=" + identifiant + "]";
    }

}
