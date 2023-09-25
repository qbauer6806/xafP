package mc.gouv.xaf.shared.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Modélise un fichier lié à une demande
 *
 * @author qdeme
 */
public class DemandeFileDTO {

	protected Integer pkDemandesFiles;
	@NotNull
	protected String name;
	@NotNull
	protected String url;
	protected String meta;
	protected Date date;
	protected String identifiant;
	protected String typedoc;
	private Integer pkDemandesComplementsFiles;
	
	// Dixit mboutelier.ext : "Flag pour indiquer si c'est un fichier d'une demande complémentaire"
	// Ne va pas jusqu'en base
	private boolean compFile;
	
	// Correspond à la checkbox de vérification de pièces jointes dans le BO
	// Va jusqu'en base
	private boolean verification;

	public Integer getPkDemandesFiles() {
		return pkDemandesFiles;
	}

	public void setPkDemandesFiles(Integer pkDemandesFiles) {
		this.pkDemandesFiles = pkDemandesFiles;
	}

	public Integer getPkDemandesComplementsFiles() {
		return pkDemandesComplementsFiles;
	}

	public void setPkDemandesComplementsFiles(Integer pkDemandesComplementsFiles) {
		this.pkDemandesComplementsFiles = pkDemandesComplementsFiles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public String getTypedoc() {
		return typedoc;
	}

	public void setTypedoc(String typedoc) {
		this.typedoc = typedoc;
	}

	public boolean isCompFile() {
		return compFile;
	}

	public void setCompFile(boolean compFile) {
		this.compFile = compFile;
	}

	public boolean isVerification() {
		return verification;
	}

	public void setVerification(boolean verification) {
		this.verification = verification;
	}
}
