package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Changement de statut simplifié d'une demande
 *
 * Sens : TS -> GU (topic ts-to-gichuni)
 *
 * Dans les TS, à chaque demande correspond un statut public (montré à l'usager), et parfois aussi un statut interne
 * (visible uniquement des agents). Dans tous les cas, le statut public est une notion propre au TS, et le GU ne peut
 * pas les comprendre. D'où l'idée de communiquer un statut simplifié au GU, parmi 3 possibilités :
 *
 * - En cours ("EN_COURS")
 * - En attente d'une action de l'usager ("EN_ATTENTE_USAGER"), exemple demande en attente d'informations
 * complémentaires, en attente de paiement, etc.
 * - Terminée ("TERMINEE")
 *
 * Charge aux TS de convertir son statut en statut simplifié afin de le communiquer au GU.
 *
 * Pour cela, à chaque fois que le statut simplifié d'une demande change, un message est envoyé du TS au GU.
 *
 * @author qdeme
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangementStatutDemandeMessage extends GUKafkaMessage {

    private String demarcheId;

    // En String afin d'anticiper le fait que le GU puisse avoir des ID non numériques
    private String usagerId;

    private Integer demandeId;

    private String identifiant;

    private String statutSimplifie;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date dateStatutSimplifie;

    private RecapDemandesDTO recapDemandes;

    public ChangementStatutDemandeMessage() {
        super("changement-statut-demande");
    }

    public ChangementStatutDemandeMessage(String demarcheId, String usagerId, Integer demandeId, String identifiant,
            Date dateStatutSimplifie, String statutSimplifie, RecapDemandesDTO recapDemandes) {
        this();
        this.demarcheId = demarcheId;
        this.usagerId = usagerId;
        this.demandeId = demandeId;
        this.identifiant = identifiant;
        this.dateStatutSimplifie = dateStatutSimplifie;
        this.statutSimplifie = statutSimplifie;
        this.recapDemandes = recapDemandes;
    }

}
