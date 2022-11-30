package mc.gouv.xaf.backweb.ws;

import io.jsonwebtoken.lang.Collections;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeFileEsRechercheDTO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@GouvRestController
@RequestMapping("/ws/courriers")
@Conditional(IndexationEnabledCondition.class)
public class RechercheIndexedCourriersController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheIndexedCourriersController.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private IndexedDemandeService demandesService;

    private static final String FICHIER_NAME = "courrier.fichiers.name";
    private static final String DEMANDE_IDENTIFIANT = "identifiant";
    private static final String COURRIER_DATE_RECEPTION = "courrierDateReception";
    private static final String DERNIER_STATUT_LIBELLE = "dernierStatut.libelle";
    private static final String COURRIER_CONTENT = "courrier.fichiers.content";

    @GetMapping(value = "/pageable")
    public Page<DemandeFileEsRechercheDTO> getDemandes(@RequestParam(value = "usagerId", required = false) Integer usagerId,
                                                       @RequestParam(value = "statut", required = false) List<String> statuts,
                                                       @RequestParam(value = "canal", required = false) List<DemandeCanalEnum> canaux,
                                                       @RequestParam(value = "agentId", required = false) String agentId,
                                                       @RequestParam(value = "creationStartDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationStartDate,
                                                       @RequestParam(value = "creationEndDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationEndDate,
                                                       @RequestParam(value = "texte", required = false) String texte,
                                                       @RequestParam(value = "searchFields", required = false) List<String> searchFields,
                                                       @RequestParam(value = "data", required = false) DataRechercheDTO data,
                                                       @RequestParam(value = "imprime", required = false) boolean imprime,
                                                       @RequestParam(value = "aucunCanal", required = false) boolean aucunCanal,
                                                       @RequestParam(value = "aucunStatut", required = false) boolean aucunStatut, Pageable pageable) {

        LOGGER.info("======================= Appel de /ws/demandes/pageable (statuts={}, canaux={}, agentId={}, creationStartDate={}, creationEndDate={}, texte={}, data={})",
                statuts, canaux, agentId, creationStartDate, creationEndDate, texte, data);

        DemandeCourrierRechercheDTO demandeRecherche = new DemandeCourrierRechercheDTO();
        demandeRecherche.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        demandeRecherche.setAgentAffecteId(agentId);
        demandeRecherche.setUsagerId(usagerId);
        demandeRecherche.setCreationStartDate(creationStartDate);
        demandeRecherche.setCreationEndDate(creationEndDate);
        demandeRecherche.setStatuts(statuts);
        demandeRecherche.setTexte(texte);
        demandeRecherche.setCanaux(canaux);
        demandeRecherche.setData(data);
        demandeRecherche.setIdentifiant(null);
        demandeRecherche.setAucunStatut(aucunStatut);
        demandeRecherche.setAucunCanal(aucunCanal);
        demandeRecherche.setImprime(imprime);

        populateSearchFields(demandeRecherche, searchFields);

        // TODO sort non null ?
        if (pageable.getSort() != null && !pageable.getSort().isUnsorted()) {
            Order order = pageable.getSort().iterator().next();
            if (order != null) {
                return demandesService.getIndexedCourriers(demandeRecherche, pageable, new String[] {});
            }
        }

        Pageable newPageable;
        if (StringUtils.isBlank(texte)) {

            newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC,
                    "identifiant.keyword");
        } else {
            newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        }

        LOGGER.info("======================= Fin appel de /ws/demandes/pageable");

        return demandesService.getIndexedCourriers(demandeRecherche, newPageable, new String[] {});
    }

    /**
     * Methode permettant de spécifier les nouveaux champs et ceux par default
     */
    private void populateSearchFields(DemandeRechercheDTO demandeRecherche, List<String> customSearchFields) {
        List<String> searchFields = new ArrayList<>();

        // par défault
        searchFields.add(FICHIER_NAME);
        searchFields.add(DEMANDE_IDENTIFIANT);
        searchFields.add(COURRIER_DATE_RECEPTION);
        searchFields.add(DERNIER_STATUT_LIBELLE);
        searchFields.add(COURRIER_CONTENT);

        // custom
        if (!Collections.isEmpty(customSearchFields)) {
            searchFields.addAll(customSearchFields);
        }
        demandeRecherche.setSearchFields(searchFields.toArray(new String[0]));
    }
}
