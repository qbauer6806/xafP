#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

/**
 * Référence les statuts internes du BPM (en fait, les tâches du BPM qui
 * correspondent à un état interne)
 *
 */
public enum ${artifactIdCamelCase}StatutInterneEnum {
	validationHierarchiqueTask("En attente de validation hiérarchique");

	public String libelle;

	${artifactIdCamelCase}StatutInterneEnum(String libelle) {
		this.libelle = libelle;
	}

	@Override
	public String toString() {
		return libelle;
	}
}
