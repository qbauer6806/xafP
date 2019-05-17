package mc.gouv.af.back.service.impl;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.af.back.service.RechercheAdminService;
import mc.gouv.af.back.service.RechercheDynamicJSService;
import mc.gouv.af.data.dao.RechercheChampConfigRepository;
import mc.gouv.af.data.entity.RechercheChampConfigBo;

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

        Iterable<RechercheChampConfigBo> champs = rechercheChampConfigRepository.findAll();
        if (champs != null) {
            for (RechercheChampConfigBo champBo : champs) {
                if (champBo != null && champBo.isEnabled() && champBo.getLibelle() != null) {
                    String category = champBo.getCategorie() != null ? champBo.getCategorie().getLibelle() : "Autres";
                    dynamicJsSB.append("\n").append(MessageFormat.format(RECHERCHE_LIBELLE_JS_TEMPLATE,
                            champBo.getCle(), champBo.getLibelle(), category));
                }
            }
        }
        dynamicJs = dynamicJsSB.toString();
    }

}
