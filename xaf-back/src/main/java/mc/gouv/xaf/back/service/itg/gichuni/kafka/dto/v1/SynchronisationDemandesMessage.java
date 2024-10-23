package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Message de synchronisation globale des demandes effectuées par les usagers du TS à destination du Guichet Unique via
 * Kafka.
 *
 * @author qdeme
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynchronisationDemandesMessage extends GUKafkaMessage {

    private String demarcheId;

    private List<UsagerDemandesRecapDTO> usagerDemandesRecap;

    public SynchronisationDemandesMessage() {
        super("synchronisation-demandes");
    }

    public SynchronisationDemandesMessage(String demarcheId, List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        this();
        this.demarcheId = demarcheId;
        this.usagerDemandesRecap = usagerDemandesRecap;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public List<UsagerDemandesRecapDTO> getUsagerDemandesRecap() {
        return usagerDemandesRecap;
    }

    public void setUsagerDemandesRecap(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        this.usagerDemandesRecap = usagerDemandesRecap;
    }

}
