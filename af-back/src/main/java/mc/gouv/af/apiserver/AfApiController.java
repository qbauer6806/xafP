package mc.gouv.af.apiserver;

import java.util.List;

import mc.gouv.dem.apishared.model.DemandeDTO;

public interface AfApiController {

    public List<DemandeDTO> getDemandes();
    
}
