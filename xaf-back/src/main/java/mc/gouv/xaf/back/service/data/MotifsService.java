package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.shared.dto.ExportMotifDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.formbean.MotifCreateFormBean;

/**
 * Service permettant la manipulation des motifs.
 *
 * @author qdeme
 */
public interface MotifsService {

    /**
     * Permet de récupérer le motif correspondant à un MotifID
     *
     * @return Le motif demandé
     */
    MotifDTO getMotif(Integer pkMotif);

    /**
     * <p>Permet de récupérer pour un statut donné une map contenant les motifs.</p>
     * <p>La clé de la map est un composé du code du motif et de la langue (ex: CODE_MOTIF_FR).</p>
     *
     * @return une map contenant une chaine et un motif dto
     */
    Map<String, MotifDTO> getMotifsByStatut(String statut);

    /**
     * Permet de récupérer les motifs correspondant
     *
     * @return Les motifs demandés
     */
    List<MotifDTO> getMotifs();

    List<MotifDTO> getMotifs(String langue);

    /**
     * Permet de sauvegarder ou mettre à jour un motif en base
     *
     * @param motif,
     *         DTO à sauvegarder
     * @return Le motif sauvegardé ou mis à jour
     */
    MotifDTO saveOrUpdateMotif(MotifDTO motif);

    MotifDTO desactiverMotif(String motifCode, String langue);

    MotifDTO activerMotif(String motifCode, String langue);

    MotifDTO getMotif(String codeMotif, String langue);

    List<MotifDTO> getMotifs(String langue, String statut);

    List<MotifDTO> getFilteredMotifs(String langue, List<String> codes);

    MotifDTO getMotif(String codeMotif, String langue, String statut);

    String[] getMotifPreviewByText(String bodyTemplateText, String subjectTemplateText, Map<String, Object> model)
            throws IOException;

    String exportConfig() throws IOException;

    List<ExportMotifDTO> importConfig(byte[] file) throws IOException;

    void saveMotifForm(MotifCreateFormBean formBean);

}
