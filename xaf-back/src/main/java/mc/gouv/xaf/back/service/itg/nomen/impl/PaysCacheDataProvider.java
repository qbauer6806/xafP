package mc.gouv.xaf.back.service.itg.nomen.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.nomen.dto.NomenNomenclatureDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurValeurParametreDTO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.util.PaysUtils;

/**
 * DataProvider du cache des pays et nationalités
 * Il appelle l'API NOMEN pour récupérer les données et les mettre en cache
 *
 * @author qdeme
 */
@Component
public class PaysCacheDataProvider implements GouvCacheDataProvider<String, PaysDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysCacheDataProvider.class);

    private static final String CODE_ALPHA3_PARAMETRE = "CODE_ALPHA3";
    
    private static final String CODE_ALPHA2_APATRIDE = "XX";
    
    private static final String CODE_ALPHA3_APATRIDE = "XXA";
    
    private static final String NOMENCLATURE_PAYS = "PAY-1";
    
    private static final String NOMENCLATURE_NATIONALITES = "NATIO";
    
    private static final String LANGUE_FR = "FR";
    
    private static final String LANGUE_EN = "EN";

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public ConcurrentHashMap<String, PaysDTO> getAll() {

        LOGGER.info("Appel de l'API NOMEN (PAY-1,FR)...");
        ConcurrentHashMap<String, PaysDTO> map = new ConcurrentHashMap<>();
        NomenNomenclatureDTO nomenclature = afBackUtils.getNomenClient().getNomenclatureAvecLocale(NOMENCLATURE_PAYS, LANGUE_FR);
        for (NomenValeurDTO valeur : nomenclature.getValeurs()) {
            PaysDTO pays = getPaysFromNomenValeur(valeur);
            map.put(pays.getCode(), pays);
        }

        LOGGER.info("Appel de l'API NOMEN (PAY-1,EN)...");
        NomenNomenclatureDTO nomenclatureEn = afBackUtils.getNomenClient().getNomenclatureAvecLocale(NOMENCLATURE_PAYS, LANGUE_EN);
        for (NomenValeurDTO valeur : nomenclatureEn.getValeurs()) {
            PaysDTO pays = map.get(valeur.getCode());
            pays.setLibelleEn(valeur.getLibelleCourt());
            pays.setLibelleLongEn(valeur.getLibelleLong());
        }
        
        LOGGER.info("Appel de l'API NOMEN (NATIO,FR)...");
        NomenNomenclatureDTO nomenclatureNatioFr = afBackUtils.getNomenClient().getNomenclatureAvecLocale(NOMENCLATURE_NATIONALITES, LANGUE_FR);
        for (NomenValeurDTO valeur : nomenclatureNatioFr.getValeurs()) {
            PaysDTO pays = map.get(valeur.getCode().toUpperCase());
            if (pays == null && valeur.getCode().equalsIgnoreCase(CODE_ALPHA2_APATRIDE)) {
                pays = getNationaliteFromNomenValeur(valeur);
                pays.setCodeAlpha3(CODE_ALPHA3_APATRIDE);
                map.put(valeur.getCode().toUpperCase(), pays);
            }
        }

        LOGGER.info("Appel de l'API NOMEN (NATIO,EN)...");
        NomenNomenclatureDTO nomenclatureNatioEn = afBackUtils.getNomenClient().getNomenclatureAvecLocale(NOMENCLATURE_NATIONALITES, LANGUE_EN);
        for (NomenValeurDTO valeur : nomenclatureNatioEn.getValeurs()) {
            PaysDTO pays = map.get(valeur.getCode().toUpperCase());
            if (pays != null) {
                pays.setNationaliteEn(valeur.getLibelleCourt());
            }
        }

        // On surcharge la liste des pays et nationalités par des valeurs "Non connu"
        PaysDTO paysDTO = PaysUtils.initValeurNonConnue();
        map.put(paysDTO.getCode(), paysDTO);

        return map;
    }

    @Override
    public PaysDTO get(String key) {

        LOGGER.info("Appel de l'API NOMEN (PAY-1,FR), pour récupérer le pays de code {}", key);
        NomenNomenclatureDTO nomenclature = afBackUtils.getNomenClient().getNomenclatureValeurAvecLocale(NOMENCLATURE_PAYS, key, LANGUE_FR);
        NomenValeurDTO valeur = nomenclature.getValeurs().get(0);
        PaysDTO pays = getPaysFromNomenValeur(valeur);

        LOGGER.info("Appel de l'API NOMEN (PAY-1,EN), pour récupérer le pays de code {}", key);
        NomenNomenclatureDTO nomenclatureEn = afBackUtils.getNomenClient().getNomenclatureValeurAvecLocale(NOMENCLATURE_PAYS, key,
                LANGUE_EN);
        NomenValeurDTO valeurEn = nomenclatureEn.getValeurs().get(0);
        pays.setLibelleEn(valeurEn.getLibelleCourt());
        pays.setLibelleLongEn(valeurEn.getLibelleLong());
        
        LOGGER.info("Appel de l'API NOMEN (NATIO,FR), pour récupérer le pays de code {}", key);
        NomenNomenclatureDTO nomenclatureNatioFr = afBackUtils.getNomenClient().getNomenclatureValeurAvecLocale(NOMENCLATURE_NATIONALITES, key,
                LANGUE_FR);
        NomenValeurDTO valeurNatioFr = nomenclatureNatioFr.getValeurs().get(0);
        pays.setNationalite(valeurNatioFr.getLibelleCourt());

        
        LOGGER.info("Appel de l'API NOMEN (NATIO,EN), pour récupérer le pays de code {}", key);
        NomenNomenclatureDTO nomenclatureNatioEn = afBackUtils.getNomenClient().getNomenclatureValeurAvecLocale(NOMENCLATURE_NATIONALITES, key,
                LANGUE_EN);
        NomenValeurDTO valeurNatioEn = nomenclatureNatioEn.getValeurs().get(0);
        pays.setNationaliteEn(valeurNatioEn.getLibelleCourt());

        return pays;
    }

    private PaysDTO getPaysFromNomenValeur(NomenValeurDTO valeur) {
        PaysDTO pays = new PaysDTO();
        pays.setCode(valeur.getCode().toUpperCase());
        pays.setLibelle(valeur.getLibelleCourt());
        pays.setLibelleLong(valeur.getLibelleLong());
        pays.setOrdre(valeur.getOrdre());

        for (NomenValeurValeurParametreDTO param : valeur.getValeurParametres()) {
            if (CODE_ALPHA3_PARAMETRE.equals(param.getParametreNom())) {
                pays.setCodeAlpha3(param.getParametreValeur());
            }
        }

        return pays;
    }
    
    private PaysDTO getNationaliteFromNomenValeur(NomenValeurDTO valeur) {
        PaysDTO pays = new PaysDTO();
        pays.setCode(valeur.getCode().toUpperCase());
        pays.setLibelle("");
        pays.setLibelleLong("");
        pays.setNationalite(valeur.getLibelleCourt());
        pays.setNationaliteCode(valeur.getCode().toUpperCase());
        pays.setOrdre(valeur.getOrdre());

        return pays;
    }

}
