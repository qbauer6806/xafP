package mc.gouv.xaf.backweb.denjs.controller;

import mc.gouv.logon.apiclient.LogonApiClient;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.denjs.dto.DenjsAffectationAgentDTO;
import mc.gouv.xaf.back.denjs.dto.DenjsEtablissementDTO;
import mc.gouv.xaf.back.denjs.service.DenjsAffectationService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.backweb.denjs.dto.DenjsAgentEtablissementDTO;
import mc.gouv.xaf.backweb.denjs.formbean.DenjsGestionAgentsFormBean;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller de la page de gestion des agents
 *
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/denjs/gestion/agents")
@Secured("ROLE_PARAMETRAGE")
public class DenjsGestionAgentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DenjsGestionAgentsController.class);
    private static final String AFFECTATION_SUCCES = "L'affectation de l'agent a été modifiée avec succès.";

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DenjsAffectationService denjsAffectationService;

    @GetMapping
    public ModelAndView form(@ModelAttribute("denjsGestionAgentsFormBean") DenjsGestionAgentsFormBean denjsGestionAgentsFormBean,
                             final RedirectAttributes redirectAttributes) {
        LOGGER.info("Appel de la page /denjs/gestion/agents. Méthode form");
        ModelAndView mav = new ModelAndView("denjs/gestionagents/gestionagents");

        List<DenjsAffectationAgentDTO> affectations = denjsAffectationService.getAffectationsAgents();
        List<DenjsEtablissementDTO> etablissements = denjsAffectationService.getEtablissements();

        List<User> list;
        try {
            LogonApiClient logonApiClient = new LogonApiClient(gouvPropertiesResolver.getGouvSharedLogonRestUrl());
            list = logonApiClient.getRessUser().getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());

            List<DenjsAgentEtablissementDTO> agents = new ArrayList<>();
            for (User user : list) {
            	DenjsAgentEtablissementDTO agent = new DenjsAgentEtablissementDTO();
            	agent.setAgentNom(user.getNom());
            	agent.setAgentMatricule(user.getMatricule());
            	for (DenjsAffectationAgentDTO aff : affectations) {
            		if (aff.getAgentMatricule().equals(user.getMatricule())) {
            			agent.setEtablissementCode(aff.getEtablissementCode());
            			DenjsEtablissementDTO etablissement = denjsAffectationService.getEtablissementFromCode(aff.getEtablissementCode(), etablissements);
            			agent.setEtablissementNom(etablissement.getNom());
            		}
            	}
            	agents.add(agent);
            }

            mav.addObject("agents", agents);
            mav.addObject("etablissements", etablissements);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formUser.", e);
        }

        LOGGER.info("======================= Fin /denjs/gestion/agents. Méthode form");
        return mav;
    }

    @PostMapping(value = "/edit", params = "action=valider")
    @Transactional
    public ModelAndView modifier(@Valid @ModelAttribute("denjsGestionAgentsFormBean") DenjsGestionAgentsFormBean denjsGestionAgentsFormBean,
                                 BindingResult result, final RedirectAttributes redirectAttributes) {

        String agentMatricule = denjsGestionAgentsFormBean.getAgentMatricule();
        String etablissementCode = denjsGestionAgentsFormBean.getEtablissementCode();
        if ("aucun".equals(etablissementCode)) {
        	etablissementCode = null;
        }

        LOGGER.info("======================= Appel de la page /denjs/gestion/agents/edit ({}, {})", agentMatricule, etablissementCode);

        DenjsAffectationAgentDTO affectation = new DenjsAffectationAgentDTO();
        affectation.setAgentMatricule(agentMatricule);
        affectation.setEtablissementCode(etablissementCode);
        denjsAffectationService.affecterAgentEtablissement(affectation);

        ModelAndView mav = new ModelAndView("redirect:/denjs/gestion/agents");
        List<String> messages = new ArrayList<>();
        messages.add(AFFECTATION_SUCCES);
        redirectAttributes.addFlashAttribute(SharedMessages.SUCCESS_MESSAGES, messages);

        LOGGER.info("======================= Fin /denjs/gestion/agents/edit");
        return mav;
    }

}
