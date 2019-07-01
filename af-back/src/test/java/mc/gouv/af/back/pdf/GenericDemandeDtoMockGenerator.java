package mc.gouv.af.back.pdf;

import mc.gouv.dem.shared.model.DemandeDTO;

public class GenericDemandeDtoMockGenerator {

    public static final DemandeDTO getGenericMockDemandeDTO() {
        DemandeDTO dto;
        dto = new DemandeDTO();
        dto.setPkDemandes(1);
        return dto;
    }
}
