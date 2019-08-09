#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.exception;

/**
 * Classe d'exceptions spécifiques à la démarche
 * 
 * @author qdeme
 *
 */
public class ${artifactIdCamelCase}Exception extends RuntimeException {
    
    private static final long serialVersionUID = -7827082648821420881L;
    
    public ${artifactIdCamelCase}Exception(String message) {
        super(message);
    }
    
}
