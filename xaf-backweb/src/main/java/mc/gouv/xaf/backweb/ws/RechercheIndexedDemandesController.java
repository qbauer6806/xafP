package mc.gouv.xaf.backweb.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.DemandeEsRechercheDTO;
import mc.gouv.xaf.back.data.es.model.DemandesFacet;
import mc.gouv.xaf.back.data.es.model.DemandesFacets;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.back.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.back.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.dto.AfBackDemandeEsDTO;
import mc.gouv.logon.shared.User;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@RequestMapping("/ws/demandes")
@Conditional(IndexationEnabledCondition.class)
public class RechercheIndexedDemandesController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheIndexedDemandesController.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private IndexedDemandeService demandesService;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @RequestMapping(value = "/pageable", method = RequestMethod.GET)
    public Page<AfBackDemandeEsDTO> getDemandes(@RequestParam(value = "usagerId", required = false) Integer usagerId,
                                                @RequestParam(value = "statut", required = false) List<String> statuts,
                                                @RequestParam(value = "canal", required = false) List<DemandeCanalEnum> canaux,
                                                @RequestParam(value = "agentId", required = false) String agentId,
                                                @RequestParam(value = "creationStartDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationStartDate,
                                                @RequestParam(value = "creationEndDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationEndDate,
                                                @RequestParam(value = "texte", required = false) String texte,
                                                @RequestParam(value = "searchFields", required = false) String[] searchFields,
                                                @RequestParam(value = "data", required = false) DataRechercheDTO data,
                                                @RequestParam(value = "aucunCanal", required = false) boolean aucunCanal,
                                                @RequestParam(value = "aucunStatut", required = false) boolean aucunStatut, Pageable pageable) {

        LOGGER.info("======================= Appel de /ws/demandes/pageable (statuts=" + statuts + ",canaux=" + canaux
                + ",agentId=" + agentId + ",creationStartDate=" + creationStartDate + ",creationEndDate="
                + creationEndDate + ",texte=" + texte + ",data=" + data);

        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
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

        demandeRecherche.setSearchFields(searchFields);

        if (pageable.getSort() != null && !pageable.getSort().isUnsorted()) {
            Order order = pageable.getSort().iterator().next();
            if (order != null) {
                return processCustomData(
                        demandesService.getIndexedDemandes(demandeRecherche, pageable, new String[] {}));
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

        return processCustomData(demandesService.getIndexedDemandes(demandeRecherche, newPageable, new String[] {}));
    }

    @RequestMapping(value = "/facets", method = RequestMethod.GET)
    public List<DemandesFacet> getDemandesFacets(@RequestParam(value = "usagerId", required = false) Integer usagerId,
            @RequestParam(value = "statut", required = false) List<String> statuts,
            @RequestParam(value = "canal", required = false) List<DemandeCanalEnum> canaux,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "creationStartDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationStartDate,
            @RequestParam(value = "creationEndDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationEndDate,
            @RequestParam(value = "texte", required = false) String texte,
            @RequestParam(value = "data", required = false) DataRechercheDTO data) {

        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO(gouvPropertiesResolver.getDemarcheId(), texte,
                statuts, canaux, agentId, usagerId, creationStartDate, creationEndDate, data, null);

        DemandesFacets demandesFacets = demandesService.getDemandesFacets(demandeRecherche);

        return demandesFacets.getFacets();

    }

    @SuppressWarnings("deprecation")
    private Page<AfBackDemandeEsDTO> processCustomData(Page<DemandeEsRechercheDTO> demandes) {

        if (demandes == null || !demandes.hasContent()) {
            return Page.empty();
        }
        List<AfBackDemandeEsDTO> newDemandes = new ArrayList<AfBackDemandeEsDTO>();
        for (DemandeEsRechercheDTO demande : demandes) {
            AfBackDemandeEsDTO newDem = new AfBackDemandeEsDTO(demande);
            if (demande.getAgent() != null && demande.getAgent().getMatricule() != null) {
                User user = utilisateursCache.get(demande.getAgent().getMatricule());
                newDem.setAgentAffectePrenom(user.getPrenom());
                newDem.setAgentAffecteNom(user.getNomAffichage());
            }
            newDemandes.add(newDem);
        }
        Pageable newPageable = new PageRequest(demandes.getNumber(), demandes.getSize(), demandes.getSort());
        return new PageImpl<>(newDemandes, newPageable, demandes.getTotalElements());
    }

}
