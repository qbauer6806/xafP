package mc.gouv.xaf.back.service.motifs.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.MotifsRepository;
import mc.gouv.xaf.back.data.entity.MotifBO;
import mc.gouv.xaf.back.data.transformer.MotifTransformer;
import mc.gouv.xaf.back.service.motifs.GestionMotifsService;
import mc.gouv.xaf.shared.dto.ExportMotifDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class GestionsMotifsServiceImpl implements GestionMotifsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionsMotifsServiceImpl.class);

    private final MotifsRepository motifsRepository;

    @Override
    public String exportConfig() throws IOException {

        LOGGER.info("Début de l'export de la configuration des templates");

        List<MotifDTO> motifDTOS = MotifTransformer.bo2Dto(motifsRepository.findAll());

        // Convertir en fichier d'export
        List<ExportMotifDTO> exportTemplateList = new ArrayList<>();
        for (MotifDTO motifDTO : motifDTOS) {
            ExportMotifDTO exportMotifDTO = new ExportMotifDTO();
            exportMotifDTO.setCode(motifDTO.getCode());
            exportMotifDTO.setLibelle(motifDTO.getLibelle());
            exportMotifDTO.setStatut(motifDTO.getStatut());
            exportMotifDTO.setStatutCourant(motifDTO.getStatutCourant());
            exportMotifDTO.setLangue(motifDTO.getLangue());
            exportMotifDTO.setDateArchive(motifDTO.getDateArchive());
            exportMotifDTO.setCommentairePrerempli(motifDTO.getCommentairePrerempli());
            exportMotifDTO.setTexteAEnvoyer(motifDTO.getTexteAEnvoyer());
            exportTemplateList.add(exportMotifDTO);
        }

        ObjectMapper mapper = new ObjectMapper();
        String exportedConfig = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportTemplateList);
        LOGGER.debug("Fin de l'export de la configuration des templates, fichier exporté {}", exportedConfig);
        return exportedConfig;
    }

    @Override
    public List<ExportMotifDTO> importConfig(byte[] file) throws IOException {

        LOGGER.info("Début de l'import de la configuration");

        ObjectMapper mapper = new ObjectMapper();
        List<ExportMotifDTO> config;
        try {
            config = mapper.readValue(file, new TypeReference<>() {

            });
        } catch (StreamReadException | DatabindException e) {
            throw new DemarcheException("Le fichier ne respecte pas la structure des fichiers à importer");
        }

        if (config != null) {
            motifsRepository.deleteAll();
            Iterable<MotifBO> saved = motifsRepository.saveAll(MotifTransformer.exportDto2Bo(config));
            List<MotifBO> configBo = StreamSupport.stream(saved.spliterator(), false).collect(Collectors.toList());

            LOGGER.info("Fin de l'import de la configuration");

            return MotifTransformer.bo2ExportDto(configBo);
        }

        LOGGER.info("La configuration n'a pas pu être importée");
        return null;
    }

}
