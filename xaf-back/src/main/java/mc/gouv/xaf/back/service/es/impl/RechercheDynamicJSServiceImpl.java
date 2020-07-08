package mc.gouv.xaf.back.service.es.impl;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.dao.RechercheChampConfigRepository;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.es.RechercheAdminService;
import mc.gouv.xaf.back.service.es.RechercheDynamicJSService;
import mc.gouv.xaf.back.service.utils.HTMLEscapeUtils;

@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class RechercheDynamicJSServiceImpl implements RechercheDynamicJSService {

    @Autowired
    IndexedDemandeService indexedDemandeService;

    @Autowired
    RechercheAdminService rechercheAdminService;

    @Autowired
    RechercheChampConfigRepository rechercheChampConfigRepository;

    private static final String RECHERCHE_LIBELLE_JS_TEMPLATE = "recherche_libelles.set(\"{0}\", '{'"
            + "libelle : \"{1}\", categorie : \"{2}\"'}');";

    private String dynamicJs;

    @Override
    public String getResponse() {

        if (dynamicJs == null || dynamicJs.isEmpty()) {
            createJsFile();
        }

        return dynamicJs;
    }

    @Override
    public void createJsFile() {
        StringBuilder dynamicJsSB = new StringBuilder("var recherche_libelles = new Map();");

        Iterable<RechercheChampConfigBO> champs = rechercheChampConfigRepository.findAll();
        if (champs != null) {
            for (RechercheChampConfigBO champBo : champs) {
                if (champBo != null && champBo.isEnabled() && champBo.getLibelle() != null) {
                    String category = champBo.getCategorie() != null
                            ? HTMLEscapeUtils.escape(champBo.getCategorie().getLibelle())
                            : "Autres";
                    String escapedLibelle = HTMLEscapeUtils.escape(champBo.getLibelle());
                    String jsLine = MessageFormat.format(RECHERCHE_LIBELLE_JS_TEMPLATE, champBo.getCle(),
                            escapedLibelle, category);
                    dynamicJsSB.append("\n").append(jsLine);
                }
            }
        }
        dynamicJs = dynamicJsSB.toString();
    }

}
