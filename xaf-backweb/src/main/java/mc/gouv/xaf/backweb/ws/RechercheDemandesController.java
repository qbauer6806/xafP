package mc.gouv.xaf.backweb.ws;

import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.config.es.IndexationDisabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.dto.AfBackDemandeDTO;
import mc.gouv.xaf.shared.dto.DataRechercheDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xboot.config.web.annotation.GouvRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.*;
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
@RequestMapping("/ws/demandes")
@Conditional(IndexationDisabledCondition.class)
public class RechercheDemandesController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheDemandesController.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private UtilisateursCache utilisateursCache;

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
            @RequestParam(value = "checkTimestamp", required = false, defaultValue = "false") boolean checkTimestamp,
            Pageable pageable) {

        LOGGER.info(
                "======================= Appel de /ws/demandes/pageable (statuts={}, canaux={}, agentId={}, creationStartDate={}, creationEndDate={}, texte={}, data={})",
                statuts, canaux, agentId, creationStartDate, creationEndDate, texte, data);

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
        demandeRecherche.setAucunCanal(aucunCanal);
        demandeRecherche.setAucunStatut(aucunStatut);
        demandeRecherche.setCheckTimestamp(checkTimestamp);

        Order order = pageable.getSort().iterator().next();
        if (order != null) {
            return processCustomData(demandesService.getDemandes(demandeRecherche, pageable, new String[] {}));
        }

        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC,
                "identifiant");

        LOGGER.info("======================= Fin appel de /ws/demandes/pageable");

        return processCustomData(demandesService.getDemandes(demandeRecherche, newPageable, new String[] {}));
    }

    private Page<AfBackDemandeDTO> processCustomData(Page<DemandeDTO> demandes) {
        List<AfBackDemandeDTO> newDemandes = new ArrayList<>();
        for (DemandeDTO demande : demandes) {
            AfBackDemandeDTO newDem = new AfBackDemandeDTO(demande);
            if (demande.getAgentAffecteId() != null) {
                User user = utilisateursCache.get(demande.getAgentAffecteId());
                newDem.setAgentAffectePrenom(user.getPrenom());
                newDem.setAgentAffecteNom(user.getNomAffichage());
            }
            newDemandes.add(newDem);
        }
        Pageable newPageable = PageRequest.of(demandes.getNumber(), demandes.getSize(), demandes.getSort());
        return new PageImpl<>(newDemandes, newPageable, demandes.getTotalElements());
    }
}
