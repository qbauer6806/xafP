package mc.gouv.xaf.shared.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Modélise un fichier lié à un brouillon d'une demande
 *
 * @author qdeme
 */
public class BrouillonFileDTO {

	protected Integer pkBrouillonsFiles;
	
	@NotNull
	protected String name;
	
	@NotNull
	protected String url;
	
	protected String meta;
	
	protected Date date;
	
	protected String typedoc;

	public Integer getPkBrouillonsFiles() {
		return pkBrouillonsFiles;
	}

	public void setPkBrouillonsFiles(Integer pkBrouillonsFiles) {
		this.pkBrouillonsFiles = pkBrouillonsFiles;
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

	public String getTypedoc() {
		return typedoc;
	}

	public void setTypedoc(String typedoc) {
		this.typedoc = typedoc;
	}

	@Override
	public String toString() {
		return "BrouillonFileDTO [pkBrouillonsFiles=" + pkBrouillonsFiles + ", name=" + name + ", url=" + url
				+ ", date=" + date + "]";
	}

}
