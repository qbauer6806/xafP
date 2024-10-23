package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM_KAFKA_OUTBOX
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_KAFKA_OUTBOX")
public class KafkaOutboxBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_KAFKAOUTBOX", nullable = false)
    private Integer pkKafkaOutbox;

    @Column(name = "TOPIC", length = 256, nullable = false)
    @Size(min = 1, max = 256)
    private String topic;

    @Column(name = "\"KEY\"", length = 256, nullable = false)
    @Size(max = 256)
    private String key;

    @Column(name = "CONTENU", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String contenu;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_LAST_ATTEMPT")
    private Date dateLastAttempt;

    @Column(name = "NB_FAILED_ATTEMPTS", nullable = false)
    private Integer nbFailedAttempts;

    @Column(name = "STATUT", length = 128, nullable = false)
    @Size(min = 1, max = 128)
    private String statut;

}
