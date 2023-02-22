package mc.gouv.xaf.back.pdf;

import mc.gouv.xaf.shared.dto.DemandeDTO;

public class GenericDemandeDtoMockGenerator {

    public static DemandeDTO getGenericMockDemandeDTO() {
        DemandeDTO dto;
        dto = new DemandeDTO();
        dto.setPkDemandes(1);
        return dto;
    }
}
