package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.MotifsRepository;
import mc.gouv.xaf.back.data.entity.MotifBO;
import mc.gouv.xaf.back.data.transformer.MotifTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.ExportMotifDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xaf.shared.formbean.MotifCreateFormBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

/**
 * Service permettant la manipulation des motifs.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MotifsServiceImpl implements MotifsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsServiceImpl.class);

    private final MotifsRepository motifsRepository;
    private final ToolManager manager;
    private final VelocityEngine velocityEngine;

    private MotifBO getMotifBO(Integer pkMotif) {
        return motifsRepository.findById(pkMotif).orElseThrow(
                () -> new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO getMotif(Integer pkMotif) {
        MotifBO motifBo = getMotifBO(pkMotif);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return MotifTransformer.bo2Dto(motifBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, MotifDTO> getMotifsByStatut(String statut) {
        LOGGER.info("getMotifs({})...", statut);
        List<MotifBO> motifBos = motifsRepository.findByStatut(statut);
        return motifBos.stream().collect(
                Collectors.toMap(m -> m.getCode() + '_' + m.getLangue().toUpperCase(), MotifTransformer::bo2Dto));
    }

    @Override
    public List<MotifDTO> getMotifs() {
        return motifsRepository.findAll().stream().map(MotifTransformer::bo2Dto).toList();
    }

    @Override
    public List<MotifDTO> getMotifs(String langue) {
        return motifsRepository.findByLangueAndDateArchiveIsNull(langue).stream().map(MotifTransformer::bo2Dto)
                .toList();
    }

    @Override
    public List<MotifDTO> getMotifs(String langue, String statut) {
        return motifsRepository.findByLangueAndStatutAndDateArchiveIsNull(langue, statut).stream()
                .map(MotifTransformer::bo2Dto).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO saveOrUpdateMotif(MotifDTO motif) {
        if (motif.getPkMotifs() != null) {
            // PkMotifs fourni, il faut donc mettre à jour un motif
            return updateMotif(motif);
        } else {
            // Pas de PkMotifs fourni, il faut donc créer un nouveau motif
            return saveMotif(motif);
        }
    }

    public MotifDTO saveMotif(MotifDTO motif) {
        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        MotifBO bo = MotifTransformer.dto2Bo(motif);
        bo = motifsRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return MotifTransformer.bo2Dto(bo);
    }

    public MotifDTO updateMotif(MotifDTO motif) {
        MotifBO motifBo = getMotifBO(motif.getPkMotifs());
        LOGGER.info("Mise à jour du motif...");
        motifBo.setLangue(motif.getLangue());
        motifBo.setLibelle(motif.getLibelle());
        motifBo.setStatut(motif.getStatut());
        motifBo.setStatutCourant(motif.getStatutCourant());
        motifBo.setCommentairePrerempli(motif.getCommentairePrerempli());
        motifBo.setTexteAEnvoyer(motif.getTexteAEnvoyer());
        motifBo.setCode(motif.getCode());
        // Seul l'appel DELETE permet l'inscription d'une DATE_ARCHIVE
        // La mise à jour permet seulement la mise à null afin de réactiver le motif
        if (motif.getDateArchive() == null) {
            motifBo.setDateArchive(null);
        }
        motifBo = motifsRepository.save(motifBo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        MotifDTO ret = MotifTransformer.bo2Dto(motifBo);
        ret.setUpdated(true);
        return ret;
    }

    @Override
    public MotifDTO desactiverMotif(String codeMotif, String lang) {
        MotifBO motifBo = motifsRepository.findByCodeAndLangue(codeMotif, lang).orElseThrow(
                () -> new IllegalArgumentException("Motif introuvable : " + codeMotif + " (" + lang + ")"));

        motifBo.setDateArchive(new Date());

        return MotifTransformer.bo2Dto(motifsRepository.save(motifBo));
    }

    @Override
    public MotifDTO activerMotif(String codeMotif, String lang) {
        MotifBO motifBo = motifsRepository.findByCodeAndLangue(codeMotif, lang).orElseThrow(
                () -> new IllegalArgumentException("Motif introuvable : " + codeMotif + " (" + lang + ")"));

        motifBo.setDateArchive(null);

        return MotifTransformer.bo2Dto(motifsRepository.save(motifBo));
    }

    @Override
    public MotifDTO getMotif(String codeMotif, String langue) {
        return motifsRepository.findByCodeAndLangue(codeMotif, langue).map(MotifTransformer::bo2Dto).orElse(null);
    }

    @Override
    public List<MotifDTO> getFilteredMotifs(String langue, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return motifsRepository.findByLangueAndCodeInAndDateArchiveIsNull(langue, codes).stream()
                .map(MotifTransformer::bo2Dto).toList();
    }

    @Override
    public MotifDTO getMotif(String codeMotif, String langue, String statut) {
        return motifsRepository.findByCodeAndLangueAndStatut(codeMotif, langue, statut).map(MotifTransformer::bo2Dto)
                .orElse(null);
    }

    private Context getContext() {
        Context context = manager.createContext();
        context.put("StringUtils", StringUtils.class);
        return context;
    }

    @Override
    public String[] getMotifPreviewByText(String commentairePrerempli, String texteAEnvoyer,
            Map<String, Object> model) {

        Context context = getContext();
        if (model != null) {
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        StringWriter output = new StringWriter();
        if (!velocityEngine.evaluate(context, output, "COMMENTAIRE", commentairePrerempli)) {
            throw new DemarchesServiceException(
                    "Velocity.evaluate() pour le contenu du commentairePrerempli n'a pas fonctionné.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String commentairePrerempliToSend = output.toString();
        output = new StringWriter();
        if (!velocityEngine.evaluate(context, output, "TEXTE", texteAEnvoyer)) {
            throw new DemarchesServiceException(
                    "Velocity.evaluate() pour le contenu du texteAEnvoyer n'a pas fonctionné.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String texteAEnvoyerToSend = output.toString();

        return new String[] { commentairePrerempliToSend, texteAEnvoyerToSend };
    }

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

    @jakarta.transaction.Transactional
    @Override
    public void saveMotifForm(MotifCreateFormBean formBean) {

        // Motif FR
        MotifDTO templateObjet = new MotifDTO();
        templateObjet.setCode(formBean.getCode());
        templateObjet.setStatut(formBean.getStatut());
        templateObjet.setLibelle(formBean.getLibelleFr());
        templateObjet.setCommentairePrerempli(formBean.getCommentairePrerempliFr());
        templateObjet.setTexteAEnvoyer(formBean.getTexteAEnvoyerFr());
        templateObjet.setLangue("fr");

        saveOrUpdateMotif(templateObjet);
        if (StringUtils.isNotBlank(formBean.getLibelleEn())) {
            // Motif EN
            MotifDTO templateObjetEn = new MotifDTO();
            templateObjetEn.setCode(formBean.getCode());
            templateObjetEn.setStatut(formBean.getStatut());
            templateObjetEn.setLibelle(formBean.getLibelleEn());
            templateObjetEn.setCommentairePrerempli(formBean.getCommentairePrerempliEn());
            templateObjetEn.setTexteAEnvoyer(formBean.getTexteAEnvoyerEn());
            templateObjetEn.setLangue("en");
            saveOrUpdateMotif(templateObjetEn);

        }
    }



}
