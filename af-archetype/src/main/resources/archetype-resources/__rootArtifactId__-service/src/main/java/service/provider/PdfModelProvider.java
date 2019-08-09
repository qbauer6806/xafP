#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.provider;

import mc.gouv.dem.shared.model.DemandeDTO;

public interface PdfModelProvider {

    public String xmlModelGenerator(DemandeDTO demandeDTO) throws Exception;
}
