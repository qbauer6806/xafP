package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Sens : TS -> GU (topic ts-to-gichuni)
 *
 * L'implémentation de la fonctionnalité de purge des demandes entraîne la suppression de certaines demandes du
 * téléservice. Le cas échéant, le TS envoie au GU le message suivant.
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SuppressionDemandeMessage extends GUKafkaMessage {

    private String demarcheId;

    // En String afin d'anticiper le fait que le GU puisse avoir des ID non numériques
    private String usagerId;

    private Integer demandeId;

    private String identifiant;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date dateSuppression;

    private RecapDemandesDTO recapDemandes;

    public SuppressionDemandeMessage() {
        super("suppression-demande");
    }

    public SuppressionDemandeMessage(String demarcheId, String usagerId, Integer demandeId, String identifiant,
            Date dateSuppression, RecapDemandesDTO recapDemandes) {
        this();
        this.demarcheId = demarcheId;
        this.usagerId = usagerId;
        this.demandeId = demandeId;
        this.identifiant = identifiant;
        this.dateSuppression = dateSuppression;
        this.recapDemandes = recapDemandes;
    }

}
