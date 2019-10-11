#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.provider;

import mc.gouv.dem.shared.model.DemandeDTO;

public interface PdfModelProvider {

    public String xmlModelGenerator(DemandeDTO demandeDTO) throws Exception;
}
