package mc.gouv.af.backweb.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.backweb.controller.AbstractController;
import mc.gouv.af.backweb.dto.AfBackDemandeDTO;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.service.model.DemandeRechercheDTO;
import mc.gouv.dem.shared.model.DataRechercheDTO;
import mc.gouv.dem.shared.model.DemandeCanalEnum;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.logon.shared.User;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@RequestMapping("/ws/demandes")
public class RechercheDemandesController extends AbstractController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheDemandesController.class);

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemandesService demandesService;
	
	@Autowired
    private UtilisateursCache utilisateursCache;

	@RequestMapping(value = "/pageable", method = RequestMethod.GET)
	public Page<AfBackDemandeDTO> getDemandes(@RequestParam(value = "usagerId", required = false) Integer usagerId,
			@RequestParam(value = "statut", required = false) List<String> statuts,
			@RequestParam(value = "canal", required = false) List<DemandeCanalEnum> canaux,
			@RequestParam(value = "agentId", required = false) String agentId,
			@RequestParam(value = "creationStartDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationStartDate,
			@RequestParam(value = "creationEndDate", required = false) @DateTimeFormat(iso = ISO.DATE) Date creationEndDate,
			@RequestParam(value = "texte", required = false) String texte,
			@RequestParam(value = "data", required = false) DataRechercheDTO data, Pageable pageable) {
	    
	    LOGGER.info("======================= Appel de /ws/demandes/pageable (statuts=" + statuts + ",canaux=" + canaux + ",agentId=" + agentId +
	            ",creationStartDate=" + creationStartDate + ",creationEndDate=" + creationEndDate + ",texte=" + texte + ",data=" + data);

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

		if (pageable.getSort() != null) {
			Order order = pageable.getSort().iterator().next();
			if (order != null) {
				return processCustomData(demandesService.getDemandes(demandeRecherche, pageable, new String[] {}));
			}
		}

		Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC,
				"identifiant");
		
		LOGGER.info("======================= Fin appel de /ws/demandes/pageable");
		
		return processCustomData(demandesService.getDemandes(demandeRecherche, newPageable, new String[] {}));
	}
	
    private Page<AfBackDemandeDTO> processCustomData(Page<DemandeDTO> demandes) {
        List<AfBackDemandeDTO> newDemandes = new ArrayList<AfBackDemandeDTO>();
        for (DemandeDTO demande : demandes) {
            AfBackDemandeDTO newDem = new AfBackDemandeDTO(demande);
            if (demande.getAgentAffecteId() != null) {
                User user = utilisateursCache.get(demande.getAgentAffecteId());
                newDem.setAgentAffectePrenom(user.getPrenom());
                newDem.setAgentAffecteNom(user.getNomAffichage());
            }
            newDemandes.add(newDem);
        }
        Pageable newPageable = new PageRequest(demandes.getNumber(), demandes.getSize(), demandes.getSort());
        Page<AfBackDemandeDTO> newPage = new PageImpl<AfBackDemandeDTO>(newDemandes, newPageable, demandes.getTotalElements());
        return newPage;
    }

}
