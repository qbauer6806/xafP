package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Sens : TS -> GU (topic ts-to-gichuni)
 *
 * Un usager vient de créer une demande sur un TS, le TS envoie un message au GU afin de le notifier de cette création,
 * en lui rappelant ses nombres totaux de demandes sur ce TS (évitant donc au GU de gérer des compteurs à incrémenter).
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationDemandeMessage extends GUKafkaMessage {

    private String demarcheId;

    // En String afin d'anticiper le fait que le GU puisse avoir des ID non numériques
    private String usagerId;

    private Integer demandeId;

    private String identifiant;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date dateCreation;

    private String statutSimplifie;

    private RecapDemandesDTO recapDemandes;

    public CreationDemandeMessage() {
        super("creation-demande");
    }

    public CreationDemandeMessage(String demarcheId, String usagerId, Integer demandeId, String identifiant,
            Date dateCreation, String statutSimplifie, RecapDemandesDTO recapDemandes) {
        this();
        this.demarcheId = demarcheId;
        this.usagerId = usagerId;
        this.demandeId = demandeId;
        this.identifiant = identifiant;
        this.dateCreation = dateCreation;
        this.statutSimplifie = statutSimplifie;
        this.recapDemandes = recapDemandes;
    }

}
