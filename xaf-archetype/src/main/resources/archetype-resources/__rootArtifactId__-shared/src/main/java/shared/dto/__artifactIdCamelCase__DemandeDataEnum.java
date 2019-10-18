#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

/**
 * Modélise les clés propres à la démarche ${artifactIdUpper}, associées à une demande dans DEM
 * 
 * @author mpavone
 *
 */
public enum ${artifactIdCamelCase}DemandeDataEnum {

    //FLAG 0/1 pour savoir si la demande est en attente de validation
    IS_EN_ATTENTE_VALIDATION

}
