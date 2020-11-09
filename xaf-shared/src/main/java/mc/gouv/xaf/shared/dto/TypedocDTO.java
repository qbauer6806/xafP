package mc.gouv.xaf.shared.dto;

/**
 * DTO pour la gestion des types de documents
 *
 * @author mboutelier.ext
 */
public class TypedocDTO {

	private String key;

	private String value;

	private boolean enabled;

	private boolean resident;

	public TypedocDTO(String key, String value, boolean enabled, boolean resident) {
		this.key = key;
		this.value = value;
		this.enabled = enabled;
		this.resident = resident;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isResident() {
		return resident;
	}

	public void setResident(boolean resident) {
		this.resident = resident;
	}
}
