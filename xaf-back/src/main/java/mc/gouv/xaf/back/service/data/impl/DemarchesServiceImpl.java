package mc.gouv.xaf.back.service.data.impl;

import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.transformer.DemarchesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des démarches.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemarchesServiceImpl implements DemarchesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemarchesServiceImpl.class);

    private final DemarchesRepository demarchesRepository;

    @Override
    public DemarcheDTO getDemarche() {
        DemarchesBO demarcheBo = getCheckDemarche();
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemarchesTransformer.bo2Dto(demarcheBo);
    }

    @Override
    public DemarcheDTO updateDemarche(DemarcheDTO demarche) {
        LOGGER.info("Mise à jour de la démarche...");
        DemarchesBO demarcheBo = DemarchesTransformer.dto2Bo(demarche);
        demarcheBo = demarchesRepository.save(demarcheBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemarchesTransformer.bo2Dto(demarcheBo);
    }

    @Override
    public DemarchesBO getCheckDemarche() {
        LOGGER.info("Récupération en base de la démarche...");
        Optional<DemarchesBO> demarcheBoOp = demarchesRepository.findTopBy();
        if (demarcheBoOp.isEmpty()) {
            throw new DemarchesServiceException("La démarche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        return demarcheBoOp.get();
    }

    @Override
    public String exportConfig() throws IOException {
        DemarcheDTO demarcheDTO = getDemarche();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(demarcheDTO);
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        DemarcheDTO demarche = mapper.readValue(file, DemarcheDTO.class);
        if (demarche != null) {
            updateDemarche(demarche);
        }

    }

    @Override
    public Map<String, String> getLanguesDisponibles() {
        DemarcheDTO demarche = getDemarche();
        Map<String, String> langues = new HashMap<>();
        if (demarche.getLangues().contains("fr")) {
            langues.put("fr", "Français");
        }
        if (demarche.getLangues().contains("en")) {
            langues.put("en", "Anglais");
        }
        return langues;
    }

}
