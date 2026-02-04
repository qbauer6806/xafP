package mc.gouv.xaf.xaf12batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.StreamSupport;
import mc.gouv.xaf.xaf12batch.demandes.DemandesRepository;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesComplementsFilesBO;
import mc.gouv.xaf.xaf12batch.dto.DemandesFilesBO;
import mc.gouv.xaf.xaf12batch.file.DemandesComplementsFilesRepository;
import mc.gouv.xaf.xaf12batch.file.DemandesFilesRepository;
import mc.gouv.xaf.xaf12batch.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DuplicateFilesTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateFilesTasklet.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesFilesRepository demandesFilesRepository;

    @Autowired
    private DemandesComplementsFilesRepository DemandesComplementsFilesRepository;

    @Override
    @Transactional
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        LOGGER.info("Début de la duplication des PJ");
        LOGGER.info("Début de la partie demandesFilesRepository");
        List<Integer> ids = demandesFilesRepository.findDuplicateFileIdsExcludingFirst();
        LOGGER.info("{} fichiers à dupliquer", ids.size());
        int i = 1;
        for (Integer id : ids) {

            DemandesFilesBO demandesFilesBO = demandesFilesRepository.findById(id).orElse(null);
            if (demandesFilesBO == null || demandesFilesBO.isSupprimee()) {
                continue;
            }

            DemandeBO demandeBO = demandesFilesBO.getFkDemandes();

            String oldUrl = demandesFilesBO.getUrl();
            String urlWithoutSlash = fileService.dupliquerFichier(
                    oldUrl,
                    String.valueOf(demandeBO.getPkDemandes()),
                    demandeBO.getDernierStatut().getName()
            );

            if (urlWithoutSlash == null) {
                continue;
            }

            String newUrl = urlWithoutSlash.startsWith("/") ? urlWithoutSlash : "/" + urlWithoutSlash;
            demandesFilesBO.setUrl(newUrl);

            // Mise à jour du JSON
            updateUrlsInDemande(demandeBO, oldUrl, newUrl);

            demandesFilesRepository.save(demandesFilesBO);
            demandesRepository.save(demandeBO);
            LOGGER.info("{}/{} fichiers traités", i++, ids.size());
        }
        LOGGER.info("Fin de la partie demandesFilesRepository");
        LOGGER.info("Début de la partie demandesFilesRepository");
        List<Integer> idsComplement = DemandesComplementsFilesRepository.findDuplicateComplementFileIdsExcludingFirst();
        LOGGER.info("{} fichiers compléments à dupliquer", idsComplement.size());
        i = 1;
        for (Integer id : idsComplement) {

            DemandesComplementsFilesBO demandesFilesBO = DemandesComplementsFilesRepository.findById(id).orElse(null);
            if (demandesFilesBO == null || demandesFilesBO.isSupprimee()) {
                LOGGER.info("Problème id {} : fichier absent ou supprimé", id);
                LOGGER.info("{}/{} fichiers complément traités", i++, idsComplement.size());
                continue;
            }

            DemandeBO demandeBO = demandesFilesBO.getFkDemandesComplements().getFkDemandes();

            String oldUrl = demandesFilesBO.getUrl();
            String urlWithoutSlash = fileService.dupliquerFichier(
                    oldUrl,
                    String.valueOf(demandeBO.getPkDemandes()),
                    demandeBO.getDernierStatut().getName()
            );

            if (urlWithoutSlash == null) {
                LOGGER.info("Problème id {} : impossible de récupérer le fichier", id);
                LOGGER.info("{}/{} fichiers complément traités", i++, idsComplement.size());
                continue;
            }

            String newUrl = urlWithoutSlash.startsWith("/") ? urlWithoutSlash : "/" + urlWithoutSlash;
            demandesFilesBO.setUrl(newUrl);

            DemandesComplementsFilesRepository.save(demandesFilesBO);
            LOGGER.info("{}/{} fichiers complément traités", i++, idsComplement.size());
        }
        LOGGER.info("Fin de la partie demandesFilesRepository");

        LOGGER.info("Fin de la duplication des PJ");
        return RepeatStatus.FINISHED;
    }

    private void updateUrlsInDemande(DemandeBO demandeBO, String oldUrl, String newUrl) {
        for (String path : getAllPathsFichiers(demandeBO)) {
            // on supprime contenu.
            String normalizedPath = path.replaceFirst("^contenu\\.", "");
            // on récupère le noeud dans le contenu qui correspond au fichier
            JsonNode node = resolvePath(demandeBO.getContenu(), normalizedPath);
            JsonNode nodeTrad = resolvePath(demandeBO.getContenuTrad(), normalizedPath);

            if (node != null && node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                ObjectNode objectNodeTrad = (ObjectNode) nodeTrad;

                JsonNode urlNode = objectNode.get("url");
                if (urlNode != null && objectNodeTrad != null && urlNode.isTextual() && oldUrl.equals(
                        urlNode.asText())) {
                    objectNode.put("url", newUrl);
                    objectNodeTrad.put("url", newUrl);
                }
            }
        }
    }

    private List<String> getAllPathsFichiers(DemandeBO demandeBO) {
        JsonNode fichiersConfig = demandeBO.getConfig().getContenu().get("recap").get("fichiers");
        return StreamSupport.stream(fichiersConfig.spliterator(), false)
                .flatMap(f -> StreamSupport.stream(f.path("champs").spliterator(), false))
                .flatMap(c -> StreamSupport.stream(c.path("path").spliterator(), false)).filter(JsonNode::isTextual)
                .map(JsonNode::asText).toList();
    }

    private JsonNode resolvePath(JsonNode root, String path) {
        JsonNode current = root;

        for (String part : path.split("\\.")) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return null;
            }

            // ex: cv[0]
            if (part.contains("[")) {
                String fieldName = part.substring(0, part.indexOf('['));
                int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                current = current.path(fieldName);
                if (!current.isArray() || current.size() <= index) {
                    return null;
                }
                current = current.get(index);
            } else {
                current = current.path(part);
            }
        }
        return current;
    }






}
