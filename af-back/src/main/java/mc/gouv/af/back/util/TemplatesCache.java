package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.dem.shared.model.TemplateDTO;

/**
 * Composant permettant de gérer un cache des templates de la démarche courante
 * 
 * @author qdeme
 *
 */
public interface TemplatesCache {

    public List<TemplateDTO> getTemplates();

    /**
     * Force le refresh de la liste des templates depuis le WS puis retourne la
     * liste
     * 
     * @return
     */
    public List<TemplateDTO> fetchTemplates();

    /**
     * Permet de retourner le template correspondant à un certain codeTemplate et dans
     * la langue souhaitée
     * 
     * @param codeTemplate
     * @param langue
     * @return
     */
    public TemplateDTO getTemplate(String codeTemplate, String langue);

}
