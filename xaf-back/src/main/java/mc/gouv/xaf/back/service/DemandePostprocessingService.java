package mc.gouv.xaf.back.service;

import java.io.IOException;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Interface dont l'implémentation est générée par le builder en fonction du buildId, visant à permettre la modification
 * d'une demande juste avant sa création effective, par l'API. Permet d'appliquer des règles métier issues du FO, dans
 * l'API.
 *
 * @author qdeme
 */
public interface DemandePostprocessingService {

    DemandeDTO postprocess(DemandeDTO demande) throws IOException;

}
