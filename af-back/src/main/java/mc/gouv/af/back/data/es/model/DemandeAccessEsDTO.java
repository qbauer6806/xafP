package mc.gouv.af.back.data.es.model;

public class DemandeAccessEsDTO {

    public static final String ACTIVE_FIELD_NAME = "active";
    public static final String USAGER_ID_FIELD_NAME = "usagerId";
    public static final String DEMARCHE_ID_FIELD_NAME = "demarcheId";
    public static final String FK_ACCESS_FIELD_NAME = "fkAccess";

    private Integer usagerId;
    private boolean active;
    private String demarcheId;
    private Integer fkAccess;

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public Integer getFkAccess() {
        return fkAccess;
    }

    public void setFkAccess(Integer fkAccess) {
        this.fkAccess = fkAccess;
    }

}
