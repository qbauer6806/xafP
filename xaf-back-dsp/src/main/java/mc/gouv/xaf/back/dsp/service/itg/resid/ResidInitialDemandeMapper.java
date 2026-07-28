package mc.gouv.xaf.back.dsp.service.itg.resid;

import tools.jackson.core.JacksonException;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidInitialDemandeParamDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.text.ParseException;

public interface ResidInitialDemandeMapper {

    /**
     * Permets de transformer les données reçues de RESID
     *
     * @param usagerNpdhlDTO
     *         {@link ResidUsagerNpdhlDTO} l'objet reçu de l'appel RESID
     * @param usagerId
     *         l'usager qui initialise la demande
     * @param paramDTO
     *         {@link ResidInitialDemandeParamDTO} les paramètres envoyés par l'usager
     * @return L'objet transformé en fonction du TS
     * @throws JacksonException
     * @throws ParseException
     */
    DemandeDTO mapperDonneesResid(ResidUsagerNpdhlDTO usagerNpdhlDTO, Integer usagerId, ResidInitialDemandeParamDTO paramDTO)
            throws JacksonException, ParseException;
}
