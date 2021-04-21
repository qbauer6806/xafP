package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Sens : TS -> GU
 * Message pour le Guichet Unique concernant une création de demande
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreationDemandeMessage extends GUKafkaMessage {
	
	private String demarcheId;
	
	// En String afin d'anticiper le fait que le GU puisse avoir des ID non numériques
	private String usagerId;
	
	private Integer demandeId;
	
	private String identifiant;
	
	@JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date dateCreation;
	
	private Integer nbDemandesUsager;
	
	public CreationDemandeMessage() {
		super("creation-demande");
	}
	
	public CreationDemandeMessage(String demarcheId, String usagerId, Integer demandeId,
			String identifiant, Date dateCreation, Integer nbDemandesUsager) {
		this();
		this.demarcheId = demarcheId;
		this.usagerId = usagerId;
		this.demandeId = demandeId;
		this.identifiant = identifiant;
		this.dateCreation = dateCreation;
		this.nbDemandesUsager = nbDemandesUsager;
	}

	public String getDemarcheId() {
		return demarcheId;
	}

	public void setDemarcheId(String demarcheId) {
		this.demarcheId = demarcheId;
	}

	public String getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(String usagerId) {
		this.usagerId = usagerId;
	}

	public Integer getDemandeId() {
		return demandeId;
	}

	public void setDemandeId(Integer demandeId) {
		this.demandeId = demandeId;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public Integer getNbDemandesUsager() {
		return nbDemandesUsager;
	}

	public void setNbDemandesUsager(Integer nbDemandesUsager) {
		this.nbDemandesUsager = nbDemandesUsager;
	}
	
}
