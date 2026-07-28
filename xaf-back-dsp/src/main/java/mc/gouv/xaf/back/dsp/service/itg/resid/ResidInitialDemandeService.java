package mc.gouv.xaf.back.dsp.service.itg.resid;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;

import java.text.ParseException;
import java.util.Map;

public interface ResidInitialDemandeService {

    /**
     * Permets de récupérer les données depuis RESID lors de l'initialisation d'une demande
     *
     * @param usagerId
     * @param params
     * @return
     * @throws ResidHttpResponseException
     * @throws ParseException
     * @throws JacksonException
     */
    JsonNode getInitialDemande(Integer usagerId, Map<String, String[]> params)
            throws ResidHttpResponseException, ParseException, JacksonException;
}
