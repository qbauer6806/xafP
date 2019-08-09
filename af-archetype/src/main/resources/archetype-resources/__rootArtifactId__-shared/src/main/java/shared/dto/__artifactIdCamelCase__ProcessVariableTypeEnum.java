#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

/**
 * Rassemble les valeurs possibles pour les variables spécifiques
 * au process ${artifactIdUpper} du gouvernement
 * 
 * @author qdeme
 *
 */
public enum ${artifactIdCamelCase}ProcessVariableTypeEnum {
    
    ${artifactIdUpper}_VALIDATION,
    ${artifactIdUpper}_PAR_USAGER_OU_AGENT

}
