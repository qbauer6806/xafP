package mc.gouv.xaf.back.service.motifs.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.apache.commons.lang3.StringUtils;
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
        DemarcheDTO demarcheInfos = afBackUtils.getDemarcheInfos();
        model.put("nomTs", demarcheInfos.getNom());
        model.put("nomTsEn", demarcheInfos.getNomEn());
        model.put("nomDirection", demarcheInfos.getNomDirection());
        model.put("nomSousDirection", demarcheInfos.getNomSousDirection());
        model.put("nomFooter", demarcheInfos.getNomFooter());
        model.put("adresseService", demarcheInfos.getAdresseService());
        model.put("adresseServiceInline", StringUtils.replace(demarcheInfos.getAdresseService(), "<br/>", " - "));
        model.put("nomSousDirectionComplement", demarcheInfos.getNomSousDirectionComplement());
        model.put("telephoneService", demarcheInfos.getTelephoneService());
        model.put("nomDirectionEn", demarcheInfos.getNomDirectionEn());
        model.put("nomSousDirectionEn", demarcheInfos.getNomSousDirectionEn());
        model.put("nomSousDirectionComplementEn", demarcheInfos.getNomSousDirectionComplementEn());
        model.put("urlBack", gouvPropertiesResolver.getBackUrl());
        model.put("urlFront", gouvPropertiesResolver.getFrontUrl());
        model.put("urlFicheDemarcheFr", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_FR").getValue());
        model.put("urlFicheDemarcheEn", propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "XAF_FICHE_DEMARCHE_URL_EN").getValue());
        return model;
    }

}
