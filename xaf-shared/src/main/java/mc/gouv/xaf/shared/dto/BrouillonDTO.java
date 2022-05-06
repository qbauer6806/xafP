package mc.gouv.xaf.shared.dto;

import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Modélise un brouillon d'une demande
 *
 * @author qdeme
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrouillonDTO {
	
    protected Integer pkBrouillons;
    
    private Integer fkAccess;
    
    private Integer usagerId;
    
    private String demarcheId;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateDerModif;

    protected JsonNode contenu;

    private BrouillonFileDTO[] fichiers;

    private String buildId;
    
    private String recapType;
    
    private JsonNode meta;

	public Integer getPkBrouillons() {
		return pkBrouillons;
	}

	public void setPkBrouillons(Integer pkBrouillons) {
		this.pkBrouillons = pkBrouillons;
	}

	public Integer getFkAccess() {
		return fkAccess;
	}

	public void setFkAccess(Integer fkAccess) {
		this.fkAccess = fkAccess;
	}

	public Integer getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(Integer usagerId) {
		this.usagerId = usagerId;
	}

	public String getDemarcheId() {
		return demarcheId;
	}

	public void setDemarcheId(String demarcheId) {
		this.demarcheId = demarcheId;
	}

	public Date getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	public Date getDateDerModif() {
		return dateDerModif;
	}

	public void setDateDerModif(Date dateDerModif) {
		this.dateDerModif = dateDerModif;
	}

	public JsonNode getContenu() {
		return contenu;
	}

	public void setContenu(JsonNode contenu) {
		this.contenu = contenu;
	}

	public BrouillonFileDTO[] getFichiers() {
		return fichiers;
	}

	public void setFichiers(BrouillonFileDTO[] fichiers) {
		this.fichiers = fichiers;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getRecapType() {
		return recapType;
	}

	public void setRecapType(String recapType) {
		this.recapType = recapType;
	}

	public JsonNode getMeta() {
		return meta;
	}

	public void setMeta(JsonNode meta) {
		this.meta = meta;
	}

	@Override
	public String toString() {
		return "BrouillonDTO [pkBrouillons=" + pkBrouillons + ", fkAccess=" + fkAccess + ", usagerId=" + usagerId
				+ ", demarcheId=" + demarcheId + ", dateCreation=" + dateCreation + ", dateDerModif=" + dateDerModif
				+ ", contenu=" + contenu + ", fichiers=" + Arrays.toString(fichiers) + ", buildId=" + buildId
				+ ", recapType=" + recapType + "]";
	}

}
