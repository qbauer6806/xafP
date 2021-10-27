package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM_KAFKA_OUTBOX
 * 
 * @author qdeme
 *
 */
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

    @Column(name = "KEY", length = 256, nullable = false)
    @Size(min = 0, max = 256)
    private String key;
    
    @Column(name = "CONTENU", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String contenu;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;
    
    @Column(name = "DATE_LAST_ATTEMPT", nullable = true)
    private Date dateLastAttempt;

    @Column(name = "NB_FAILED_ATTEMPTS", nullable = false)
    private Integer nbFailedAttempts;
    
    @Column(name = "STATUT", length = 128, nullable = false)
    @Size(min = 1, max = 128)
    private String statut;

	public Integer getPkKafkaOutbox() {
		return pkKafkaOutbox;
	}

	public void setPkKafkaOutbox(Integer pkKafkaOutbox) {
		this.pkKafkaOutbox = pkKafkaOutbox;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public Date getDateLastAttempt() {
		return dateLastAttempt;
	}

	public void setDateLastAttempt(Date dateLastAttempt) {
		this.dateLastAttempt = dateLastAttempt;
	}

	public Integer getNbFailedAttempts() {
		return nbFailedAttempts;
	}

	public void setNbFailedAttempts(Integer nbFailedAttempts) {
		this.nbFailedAttempts = nbFailedAttempts;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}
    
}
