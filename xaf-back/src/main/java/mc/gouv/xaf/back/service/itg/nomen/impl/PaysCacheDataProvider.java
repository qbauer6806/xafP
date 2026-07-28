package mc.gouv.xaf.back.service.itg.nomen.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.CacheService;
import mc.gouv.xaf.back.service.itg.nomen.NomenClient;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenNomenclatureDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurValeurLienDTO;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenValeurValeurParametreDTO;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.CacheDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.util.PaysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DataProvider du cache des pays et nationalités
 * Il appelle l'API NOMEN pour récupérer les données et les mettre en cache
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class PaysCacheDataProvider implements GouvCacheDataProvider<String, PaysDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaysCacheDataProvider.class);

    private static final String CODE_ALPHA3_PARAMETRE = "CODE_ALPHA3";

    private static final String CODE_ALPHA2_APATRIDE = "XX";

    private static final String CODE_ALPHA3_APATRIDE = "XXA";

    private static final String NOMENCLATURE_PAYS = "PAY-1";

    private static final String NOMENCLATURE_NATIONALITES = "NATIO";

    private static final String LANGUE_FR = "FR";

    private static final String LANGUE_EN = "EN";

    private final NomenClient nomenClient;

    private final CacheService cacheService;

    private final GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public ConcurrentHashMap<String, PaysDTO> getAll() {

        LOGGER.info("Récupération des pays en FR...");
        ConcurrentHashMap<String, PaysDTO> map = new ConcurrentHashMap<>();
        NomenNomenclatureDTO nomenclature = getPaysFromCacheDbOrApi(NOMENCLATURE_PAYS, LANGUE_FR);
        if (nomenclature != null) {
            for (NomenValeurDTO valeur : nomenclature.getValeurs()) {
                PaysDTO pays = getPaysFromNomenValeur(valeur);
                map.put(pays.getCode(), pays);
            }
        }

        LOGGER.info("Récupération des pays en EN...");
        NomenNomenclatureDTO nomenclatureEn = getPaysFromCacheDbOrApi(NOMENCLATURE_PAYS, LANGUE_EN);
        if (nomenclatureEn != null) {
            for (NomenValeurDTO valeur : nomenclatureEn.getValeurs()) {
                PaysDTO pays = map.get(valeur.getCode());
                pays.setLibelleEn(valeur.getLibelleCourt());
                pays.setLibelleLongEn(valeur.getLibelleLong());
            }
        }
        
        LOGGER.info("Récupération des nationalités en FR...");
        NomenNomenclatureDTO nomenclatureNatioFr = getPaysFromCacheDbOrApi(NOMENCLATURE_NATIONALITES, LANGUE_FR);
        if (nomenclatureNatioFr != null) {
            for (NomenValeurDTO valeur : nomenclatureNatioFr.getValeurs()) {
                PaysDTO pays = map.get(valeur.getCode().toUpperCase());
                if (pays != null) {
                    pays.setNationalite(valeur.getLibelleCourt());
                    pays.setNationaliteCode(valeur.getCode().toUpperCase());
                    for (NomenValeurValeurLienDTO lien : valeur.getValeurLiens()) {
                        if (NOMENCLATURE_PAYS.equals(lien.getLienNomenclatureCode())) {
                            PaysDTO paysReference = map.get(lien.getLienValeurCode());
                            paysReference.setNationalite(valeur.getLibelleCourt());
                            paysReference.setNationaliteCode(valeur.getCode().toUpperCase());
                        }
                    }
                } else if (valeur.getCode().equalsIgnoreCase(CODE_ALPHA2_APATRIDE)) {
                    pays = getNationaliteFromNomenValeur(valeur);
                    pays.setCodeAlpha3(CODE_ALPHA3_APATRIDE);
                    map.put(valeur.getCode().toUpperCase(), pays);
                }
            }
        }

        LOGGER.info("Récupération des nationalités en EN...");
        NomenNomenclatureDTO nomenclatureNatioEn = getPaysFromCacheDbOrApi(NOMENCLATURE_NATIONALITES, LANGUE_EN);
        if (nomenclatureNatioEn != null) {
            for (NomenValeurDTO valeur : nomenclatureNatioEn.getValeurs()) {
                PaysDTO pays = map.get(valeur.getCode().toUpperCase());
                if (pays != null) {
                    pays.setNationaliteEn(valeur.getLibelleCourt());
                    for (NomenValeurValeurLienDTO lien : valeur.getValeurLiens()) {
                        if (NOMENCLATURE_PAYS.equals(lien.getLienNomenclatureCode())) {
                            PaysDTO paysReference = map.get(lien.getLienValeurCode());
                            paysReference.setNationaliteEn(valeur.getLibelleCourt());
                        }
                    }
                }
            }
        }

        // On surcharge la liste des pays et nationalités par des valeurs "Non connu"
        PaysDTO paysDTO = PaysUtils.initValeurNonConnue();
        map.put(paysDTO.getCode(), paysDTO);

        return map;
    }

    @Override
    public PaysDTO get(String key) {
        // Cas particulier : on ne va pas appeler NOMEN à chaque clé manquante car on a une contrainte d'un seul appel par 24H
        // Et les pays/nationalités changent très très, très rarement.
        
        return null;
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
    
    /*
     * Sert à vérifier le retour API NOMEN stocké en cache DB avant d'appeler l'API si non présent ou expiré
     * Nous faisons cela car nous avons la contrainte d'appeler NOMEN une seule fois par jour et par module applicatif
     * 
     * Cf. commentaire de la classe CacheService afin d'en savoir plus sur l'utilisation de ce cache en base.
     * 
     */
    private NomenNomenclatureDTO getPaysFromCacheDbOrApi(String nomenclature, String locale) {
        CacheDTO paysDbCacheFr = cacheService.getCache(nomenclature + "_" + locale);
        long cacheDuration = gouvPropertiesResolver.getPaysCacheDuration();
        ObjectMapper mapper = new ObjectMapper();

        NomenNomenclatureDTO nomenRet = null;
        // Si la valeur n'est pas présente en base ou est expirée, appeler l'API
        if (paysDbCacheFr == null || (new Date().after(new Date(paysDbCacheFr.getDateMaj().getTime() + cacheDuration)))) {
            LOGGER.info("Appel de l'API NOMEN ({},{}) car JSON non présent en base ou expiré...", nomenclature, locale);
            nomenRet = nomenClient.getNomenclatureAvecLocale(nomenclature, locale);
            if (paysDbCacheFr == null) {
                paysDbCacheFr = new CacheDTO();
                paysDbCacheFr.setPkCache(nomenclature + "_" + locale);
            }
            paysDbCacheFr.setData(mapper.valueToTree(nomenRet));
            cacheService.updateCache(paysDbCacheFr);
        }
        else {
            try {
                nomenRet = mapper.treeToValue(paysDbCacheFr.getData(), NomenNomenclatureDTO.class);
            } catch (JacksonException | IllegalArgumentException e) {
                LOGGER.error("Erreur lors de mapper.treeToValue() dans getPaysFromCacheDbOrApi()", e);
            }
        }
        return nomenRet;
    }

}
