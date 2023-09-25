package mc.gouv.xaf.backweb.formbean;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Formulaire pour les demandes courrier
 * 
 * @author qdeme
 *
 */
public class DemandesCourrierFormBean {

	private Integer usagerId;

	@NotEmpty
	private String dateReception;

    @Size(min = 0, max = 128, message = "La référence interne ne peut contenir plus de 128 caractères")
    private String refInterne;

    @NotEmpty
	private String canal;

    @NotEmpty
	private String langue;
	private String duplicationKeyId;

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
	
	    public String getDuplicationKeyId() {
        return duplicationKeyId;
    }

    public void setDuplicationKeyId(String duplicationKeyId) {
        this.duplicationKeyId = duplicationKeyId;
    }

}
