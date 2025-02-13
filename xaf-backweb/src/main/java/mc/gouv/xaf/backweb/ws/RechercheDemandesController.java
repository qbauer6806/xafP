package mc.gouv.xaf.backweb.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.RechercheAdminService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.dto.AfBackDemandeDTO;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@GouvRestController
@Secured("ROLE_LECTURE")
@RequestMapping("/ws/demandes")
public class RechercheDemandesController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheDemandesController.class);

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private RechercheAdminService rechercheAdminService;

    @GetMapping(value = "/pageable")
    public Page<AfBackDemandeDTO> getDemandes(@RequestParam(value = "usagerId", required = false) Integer usagerId,
            @RequestParam(value = "statut", required = false) List<String> statuts,
            @RequestParam(value = "canal", required = false) List<DemandeCanalEnum> canaux,
            @RequestParam(value = "agentId", required = false) String agentId,
            @RequestParam(value = "creationStartDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationStartDate,
            @RequestParam(value = "creationEndDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationEndDate,
            @RequestParam(value = "texte", required = false) String texte,
            @RequestParam(value = "data", required = false) DataRechercheDTO data,
            @RequestParam(value = "aucunCanal", required = false) boolean aucunCanal,
            @RequestParam(value = "aucunStatut", required = false) boolean aucunStatut,
            @RequestParam(value = "aucunAgentAffecte", required = false) boolean aucunAgentAffecte,
            @RequestParam(value = "checkTimestamp", required = false, defaultValue = "false") boolean checkTimestamp,
            @RequestParam(value = "searchFields", required = false) String[] searchFields,
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "trad", required = false, defaultValue = "true") boolean trad, Pageable pageable) {
        String safeAgent = AfBackUtils.logSafe(agentId);
        String safeTexte = AfBackUtils.logSafe(texte);
        LOGGER.debug(
                "======================= Appel de /ws/demandes/pageable (canaux={}, agentId={}, creationStartDate={}, creationEndDate={}, texte={}, data={})",
                canaux, safeAgent, creationStartDate, creationEndDate, safeTexte, data);

        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setAgentAffecteId(agentId);
        demandeRecherche.setUsagerId(usagerId);
        demandeRecherche.setCreationStartDate(creationStartDate);
        demandeRecherche.setCreationEndDate(creationEndDate);
        demandeRecherche.setStatuts(statuts);
        demandeRecherche.setTexte(texte);
        demandeRecherche.setCanaux(canaux);
        demandeRecherche.setData(data);
        demandeRecherche.setIdentifiant(null);
        demandeRecherche.setAucunCanal(aucunCanal);
        demandeRecherche.setAucunStatut(aucunStatut);
        demandeRecherche.setAucunAgentAffecte(aucunAgentAffecte);
        demandeRecherche.setCheckTimestamp(checkTimestamp);
        demandeRecherche.setSearchFields(searchFields);
        demandeRecherche.setTrad(trad);

        String[] rechercheFields = fields == null ? new String[] {} : fields;

        Order order = pageable.getSort().iterator().next();
        if (order != null) {
            return processCustomData(demandesService.getDemandes(demandeRecherche, pageable, rechercheFields));
        }

        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC,
                "identifiant");

        LOGGER.info("======================= Fin appel de /ws/demandes/pageable");

        return processCustomData(demandesService.getDemandes(demandeRecherche, newPageable, rechercheFields));
    }

    private Page<AfBackDemandeDTO> processCustomData(Page<DemandeDTO> demandes) {
        List<AfBackDemandeDTO> newDemandes = new ArrayList<>();
        for (DemandeDTO demande : demandes) {
            AfBackDemandeDTO newDem = new AfBackDemandeDTO(demande);
            // todo vérifier si on peut supprimer les AfBackDemandeDTO maintenant qu'on a supprimé ES
            newDemandes.add(newDem);
        }
        Pageable newPageable = PageRequest.of(demandes.getNumber(), demandes.getSize(), demandes.getSort());
        return new PageImpl<>(newDemandes, newPageable, demandes.getTotalElements());
    }

    @GetMapping(value = "/recherchechamps")
    public List<RechercheChampDTO> getRechercheChamps() {
        LOGGER.info("Appel du webservice /ws/demandes/recherchechamps");
        return rechercheAdminService.getRechercheChamps();
    }
}
