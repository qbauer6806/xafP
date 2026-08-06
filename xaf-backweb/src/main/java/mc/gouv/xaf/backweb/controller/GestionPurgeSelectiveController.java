package mc.gouv.xaf.backweb.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.purge.PurgeDemandesService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/gestion/purgeselective")
@Secured("ROLE_CONFIGURATION")
@RequiredArgsConstructor
public class GestionPurgeSelectiveController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionPurgeSelectiveController.class);

    private final DemandesService demandesService;
    private final PurgeDemandesService purgeDemandesService;
    private final DemarchesDataProvider demarchesDataProvider;
    private final UtilisateursUtils utilisateursUtils;
    private final Environment environment;

    @GetMapping
    public ModelAndView form() {
        LOGGER.info("======================= Appel de la page /gestion/purgeselective");
        ModelAndView mav = new ModelAndView("gestion/purgeselective/purgeselective");
        mav.addObject("statuts", demarchesDataProvider.getStatusMap());
        mav.addObject("environnement", getNomEnvironnement());
        mav.addObject("prod", isProd());
        return mav;
    }

    /**
     * Endpoint DataTable server-side : renvoie une page de demandes au format attendu par DataTables.
     *
     * @param draw
     *         compteur DataTables (à renvoyer tel quel)
     * @param start
     *         index du premier élément
     * @param length
     *         taille de page
     * @param texte
     *         terme de recherche libre (optionnel)
     * @param dateDebut
     *         borne basse de la plage de dates de création (format dd/MM/yyyy, optionnel)
     * @param dateFin
     *         borne haute de la plage de dates de création (format dd/MM/yyyy, optionnel)
     */
    @GetMapping(value = "/demandes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> listerDemandes(@RequestParam(defaultValue = "1") int draw,
            @RequestParam(defaultValue = "0") int start, @RequestParam(defaultValue = "20") int length,
            @RequestParam(required = false) String texte, @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin, @RequestParam(required = false) String statut) {

        int page = length > 0 ? start / length : 0;
        Pageable pageable = PageRequest.of(page, length, Sort.by(Sort.Direction.DESC, "dateCreation"));

        DemandeRechercheDTO recherche = new DemandeRechercheDTO();
        if (texte != null && !texte.isBlank()) {
            recherche.setTexte(texte);
        }
        if (statut != null && !statut.isBlank()) {
            recherche.setStatuts(List.of(statut));
        }
        recherche.setCreationStartDate(parseDateDebut(dateDebut));
        recherche.setCreationEndDate(parseDateFin(dateFin));

        PagedModel<DemandeDTO> pageDemandes = demandesService.getDemandes(recherche, pageable,
                new String[] {"pkDemandes", "identifiant", "dateCreation", "usager", "dernierStatut", "config"});

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<Map<String, Object>> data = new ArrayList<>();
        for (DemandeDTO d : pageDemandes.getContent()) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("pkDemandes", d.getPkDemandes());
            ligne.put("identifiant", d.getIdentifiant());
            ligne.put("dateCreation", d.getDateCreation() != null ? sdf.format(d.getDateCreation()) : "");
            ligne.put("usager", getUsagerAffichage(d));
            ligne.put("statut", d.getDernierStatut() != null ? d.getDernierStatut().getLibelle() : "");
            ligne.put("buildId", getBuildIdSafe(d));
            data.add(ligne);
        }

        long total = pageDemandes.getMetadata() != null ? pageDemandes.getMetadata().totalElements() : data.size();

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("draw", draw);
        reponse.put("recordsTotal", total);
        reponse.put("recordsFiltered", total);
        reponse.put("data", data);
        return reponse;
    }

    /**
     * Purge les demandes sélectionnées (par pkDemandes). Renvoie les messages de résultat en JSON.
     */
    @PostMapping(value = "/purger", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> purger(@RequestBody PurgeRequest requete) {
        List<String> successMessages = new ArrayList<>();
        List<String> warningMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        List<Integer> pkDemandes = requete != null ? requete.pkDemandes() : null;

        // Le forçage n'est autorisé QUE hors prod, revalidé côté serveur.
        boolean forcerNonFinal = requete != null && requete.forcerNonFinal() && !isProd();

        if (pkDemandes == null || pkDemandes.isEmpty()) {
            warningMessages.add("Aucune demande sélectionnée.");
            return buildReponse(successMessages, warningMessages, errorMessages);
        }

        for (Integer pkDemande : pkDemandes) {
            if (pkDemande == null) {
                continue;
            }
            try {
                DemandeDTO demandeDTO = demandesService.getDemande(pkDemande);

                if (demandeDTO == null) {
                    warningMessages.add("La demande " + pkDemande + " n'existe pas.");
                    continue;
                }

                String identifiant = demandeDTO.getIdentifiant();

                boolean statutFinal = demarchesDataProvider.getStatutsAPurger()
                        .contains(demandeDTO.getDernierStatut().getName());

                if (!statutFinal && !forcerNonFinal) {
                    warningMessages.add(
                            "La demande " + identifiant + " n'est pas dans un statut final et n'a pas été purgée.");
                    continue;
                }

                String origineSuppression = utilisateursUtils.getUserNameFromID(AfBackUtils.getAuthenticatedAgentId());
                purgeDemandesService.deleteDemandePurgeSelective(demandeDTO.getPkDemandes(), origineSuppression);

                if (!statutFinal) {
                    successMessages.add("La demande " + identifiant
                            + " (statut non final) a été purgée [mode hors production].");
                } else {
                    successMessages.add("La demande " + identifiant + " a été purgée avec succès.");
                }

            } catch (Exception e) {
                LOGGER.error("Erreur lors de la purge de {}", pkDemande, e);
                errorMessages.add("Erreur lors de la purge de " + pkDemande + " : " + e.getMessage());
            }
        }

        if (!successMessages.isEmpty()) {
            purgeDemandesService.executerPurgeFichiers();
        }

        return buildReponse(successMessages, warningMessages, errorMessages);
    }

    /**
     * Corps de la requête de purge.
     *
     * @param pkDemandes
     *         les pk des demandes à purger
     * @param forcerNonFinal
     *         true pour purger aussi les demandes en statut non final (ignoré en prod)
     */
    public record PurgeRequest(List<Integer> pkDemandes, boolean forcerNonFinal) {
    }

    private Map<String, Object> buildReponse(List<String> successMessages, List<String> warningMessages,
            List<String> errorMessages) {
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("successMessages", successMessages);
        reponse.put("warningMessages", warningMessages);
        reponse.put("errorMessages", errorMessages);
        return reponse;
    }

    private String getUsagerAffichage(DemandeDTO d) {
        if (d.getUsager() == null) {
            return "";
        }
        String prenom = d.getUsager().getPrenom() != null ? d.getUsager().getPrenom() : "";
        String nom = d.getUsager().getNom() != null ? d.getUsager().getNom() : "";
        return (prenom + " " + nom).trim();
    }

    private String getBuildIdSafe(DemandeDTO d) {
        try {
            return d.getConfigBuildId();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Parse la borne basse : début de journée (00:00:00).
     */
    private Date parseDateDebut(String dateDebut) {
        if (dateDebut == null || dateDebut.isBlank()) {
            return null;
        }
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dateDebut.trim());
        } catch (Exception e) {
            LOGGER.warn("Purge: dateDebut invalide '{}', ignorée", dateDebut);
            return null;
        }
    }

    /**
     * Parse la borne haute : fin de journée (23:59:59) pour inclure toute la journée saisie.
     */
    private Date parseDateFin(String dateFin) {
        if (dateFin == null || dateFin.isBlank()) {
            return null;
        }
        try {
            Date d = new SimpleDateFormat("dd/MM/yyyy").parse(dateFin.trim());
            return new Date(d.getTime() + 24L * 60 * 60 * 1000 - 1000);
        } catch (Exception e) {
            LOGGER.warn("Purge: dateFin invalide '{}', ignorée", dateFin);
            return null;
        }
    }

    private String getNomEnvironnement() {
        String[] profils = environment.getActiveProfiles();
        return profils.length > 0 ? String.join(", ", profils) : "default";
    }

    private boolean isProd() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod"));
    }
}
