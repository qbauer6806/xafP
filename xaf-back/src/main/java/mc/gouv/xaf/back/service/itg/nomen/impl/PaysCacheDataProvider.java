package mc.gouv.xaf.back.service.itg.nomen.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.nomen.dto.NomenNomenclatureDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurValeurLienDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurValeurParametreDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.PaysDTO;

/**
 * DataProvider du cache des pays et nationalités
 * Il appelle l'API NOMEN pour récupérer les données
 *
 * @author qdeme
 */
@Component
public class PaysCacheDataProvider implements GouvCacheDataProvider<String, PaysDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysCacheDataProvider.class);

    private static final String CODE_ALPHA3_PARAMETRE = "CODE_ALPHA3";

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public ConcurrentHashMap<String, PaysDTO> getAll() {

        LOGGER.info("Appel (FR) de l'API NOMEN (nomenclature PAY-1), pour récupérer tous les pays et nationalités...");

        ConcurrentHashMap<String, PaysDTO> map = new ConcurrentHashMap<>();
        NomenNomenclatureDTO nomenclature = afBackUtils.getNomenClient().getNomenclature("PAY-1");
        for (NomenValeurDTO valeur : nomenclature.getValeurs()) {
            PaysDTO pays = getPaysFromNomenValeur(valeur);
            map.put(pays.getCode(), pays);
        }

        LOGGER.info("Appel (EN) de l'API NOMEN (nomenclature PAY-1), pour récupérer tous les pays et nationalités...");

        NomenNomenclatureDTO nomenclatureEn = afBackUtils.getNomenClient().getNomenclatureAvecLocale("PAY-1", "EN");
        for (NomenValeurDTO valeur : nomenclatureEn.getValeurs()) {
            PaysDTO pays = map.get(valeur.getCode());
            pays.setLibelleEn(valeur.getLibelleCourt());
            pays.setLibelleLongEn(valeur.getLibelleLong());
        }

        return map;
    }

    @Override
    public PaysDTO get(String key) {

        LOGGER.info("Appel (FR) de l'API NOMEN (nomenclature PAY-1), pour récupérer le pays de code {}", key);

        NomenNomenclatureDTO nomenclature = afBackUtils.getNomenClient().getNomenclatureValeur("PAY-1", key);
        NomenValeurDTO valeur = nomenclature.getValeurs().get(0);
        PaysDTO pays = getPaysFromNomenValeur(valeur);

        LOGGER.info("Appel (EN) de l'API NOMEN (nomenclature PAY-1), pour récupérer le pays de code {}", key);

        NomenNomenclatureDTO nomenclatureEn = afBackUtils.getNomenClient().getNomenclatureValeurAvecLocale("PAY-1", key,
                "EN");
        NomenValeurDTO valeurEn = nomenclatureEn.getValeurs().get(0);
        pays.setLibelleEn(valeurEn.getLibelleCourt());
        pays.setLibelleLongEn(valeurEn.getLibelleLong());

        return pays;
    }

    private String getNationaliteFromValeur(NomenValeurDTO valeur) {
        List<NomenValeurValeurLienDTO> liens = valeur.getValeurLiens();
        for (NomenValeurValeurLienDTO lien : liens) {
            if ("NATIO".equals(lien.getLienNomenclatureCode())) {
                return lien.getLienValeurLibelle().toUpperCase();
            }
        }
        return null;
    }

    private String getNationaliteCodeFromValeur(NomenValeurDTO valeur) {
        List<NomenValeurValeurLienDTO> liens = valeur.getValeurLiens();
        for (NomenValeurValeurLienDTO lien : liens) {
            if ("NATIO".equals(lien.getLienNomenclatureCode())) {
                return lien.getLienValeurCode().toUpperCase();
            }
        }
        return null;
    }

    private PaysDTO getPaysFromNomenValeur(NomenValeurDTO valeur) {
        PaysDTO pays = new PaysDTO();
        pays.setCode(valeur.getCode());
        pays.setLibelle(valeur.getLibelleCourt());
        pays.setLibelleLong(valeur.getLibelleLong());
        pays.setNationalite(getNationaliteFromValeur(valeur));
        pays.setNationaliteCode(getNationaliteCodeFromValeur(valeur));
        pays.setOrdre(valeur.getOrdre());

        for (NomenValeurValeurParametreDTO param : valeur.getValeurParametres()) {
            if (CODE_ALPHA3_PARAMETRE.equals(param.getParametreNom())) {
                pays.setCodeAlpha3(param.getParametreValeur());
            }
        }

        return pays;
    }

}
