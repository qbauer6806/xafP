package mc.gouv.xaf.shared.dto;

import java.util.Date;

/**
 * 
 * Modélise un élément dans la boîte d'envoi pour le Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
public class KafkaOutboxDTO {

    private Integer pkKafkaOutbox;

    private String topic;

    private String key;

    private String contenu;

    private Date dateCreation;
    
    private Date dateLastAttempt;

    private Integer nbFailedAttempts;
    
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

	@Override
	public String toString() {
		return "KafkaOutboxDTO [pkKafkaOutbox=" + pkKafkaOutbox + ", topic=" + topic + ", key=" + key + ", contenu="
				+ contenu + ", dateCreation=" + dateCreation + ", dateLastAttempt=" + dateLastAttempt + ", nbFailedAttempts="
				+ nbFailedAttempts + ", statut=" + statut + "]";
	}
    
}
