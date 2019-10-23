package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

/**
 * Modélise un fichier lié à une demande
 * 
 * @author qdeme
 *
 */
public class DemandeFileDTO {

    @NotNull
    protected String name;

    @NotNull
    protected String url;

    protected String meta;
    
    protected Date date;

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

}
