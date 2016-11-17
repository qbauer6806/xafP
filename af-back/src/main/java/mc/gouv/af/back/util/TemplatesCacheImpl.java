package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.TemplateDTO;

/**
 * Composant permettant de gérer un cache des templates de la démarche courante
 * 
 * @author qdeme
 *
 */
@Component
public class TemplatesCacheImpl implements TemplatesCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplatesCacheImpl.class);
    
    private List<TemplateDTO> cachedList = new ArrayList<TemplateDTO>();
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    private DemClient demClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateDTO> getTemplates() {
        // Initialisation du DemClient si pas déjà fait
        ensureInitialized();
        
        // Remplissage de la liste si pas déjà fait
        if (cachedList.size() == 0) {
            LOGGER.info("Récupération des templates dans DEM...");
            cachedList.addAll(demClient.getTemplates(afBackUtils.getDemarcheId()));
        }
        
        // Retour de la liste
        return cachedList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TemplateDTO> fetchTemplates() {
        // Vider la liste (forcera getTemplates() à récupérer les nouveaux du WS)
        cachedList.clear();
        
        // Retour de la nouvelle liste
        return getTemplates();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateDTO getTemplate(String codeTemplate, String langue) {
        TemplateDTO template = internalGetTemplate(codeTemplate, langue);
        if (template == null) {
            LOGGER.error("Template (" + codeTemplate + "," + langue + ") introuvable");
            if (!langue.equals("fr")) {
                template = internalGetTemplate(codeTemplate, "fr");
                if (template == null) {
                    LOGGER.error("Template (" + codeTemplate + ",fr) introuvable également");
                }
                else {
                    LOGGER.info("Template (" + codeTemplate + ",fr) trouvé et utilisé à la place");
                }
            }
        }
        return template;
    }
    
    private TemplateDTO internalGetTemplate(String codeTemplate, String langue) {
        for (TemplateDTO template : getTemplates()) {
            if (template.getCode().equals(codeTemplate) && template.getLangue().equals(langue)) {
                return template;
            }
        }
        return null;
    }
    
    /**
     * Initialisation du DemClient si pas déjà fait
     */
    private void ensureInitialized() {
        if (demClient == null) {
            demClient = new DemClient(afBackUtils.getDemUrl(), afBackUtils.getDemUser(), afBackUtils.getDemPwd());
        }
    }

}
