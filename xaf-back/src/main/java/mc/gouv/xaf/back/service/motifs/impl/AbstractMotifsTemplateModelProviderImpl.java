package mc.gouv.xaf.back.service.motifs.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public abstract class AbstractMotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public Map<String, Object> getGenericModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("nomTs", afBackUtils.getDemarcheInfos().getNom());
        model.put("nomTsEn", afBackUtils.getDemarcheInfos().getNomEn());
        model.put("nomDirection", afBackUtils.getDemarcheInfos().getNomDirection());
        model.put("nomDirectionComplement", afBackUtils.getDemarcheInfos().getNomDirectionComplement());
        model.put("nomFooter", afBackUtils.getDemarcheInfos().getNomFooter());
        model.put("adresseService", afBackUtils.getDemarcheInfos().getAdresseService());
        model.put("urlBack", gouvPropertiesResolver.getBackUrl());
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        model.put("urlFicheDemarcheFr", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_FR").getValue());
        model.put("urlFicheDemarcheEn", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_EN").getValue());
        return model;
    }

}
