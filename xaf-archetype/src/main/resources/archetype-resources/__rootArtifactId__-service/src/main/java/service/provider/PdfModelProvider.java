#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.provider;

import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface PdfModelProvider {

    String xmlModelGenerator(DemandeDTO demandeDTO) throws Exception;
}
