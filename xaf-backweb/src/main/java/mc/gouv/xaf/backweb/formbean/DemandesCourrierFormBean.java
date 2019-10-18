package mc.gouv.xaf.backweb.formbean;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Formulaire pour les demandes courrier
 * 
 * @author qdeme
 *
 */
public class DemandesCourrierFormBean {

	private Integer usagerId;

	@NotBlank
	private String dateReception;

	private String refInterne;

	@NotBlank
	private String canal;

	@NotBlank
	private String langue;

	public Integer getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(Integer usagerId) {
		this.usagerId = usagerId;
	}

	public String getDateReception() {
		return dateReception;
	}

	public void setDateReception(String dateReception) {
		this.dateReception = dateReception;
	}

	public String getRefInterne() {
		return refInterne;
	}

	public void setRefInterne(String refInterne) {
		this.refInterne = refInterne;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getLangue() {
		return langue;
	}

	public void setLangue(String langue) {
		this.langue = langue;
	}

}
