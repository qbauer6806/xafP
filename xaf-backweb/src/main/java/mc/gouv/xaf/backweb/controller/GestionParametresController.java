package mc.gouv.xaf.backweb.controller;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.backweb.dto.CustomMotifDTO;
import mc.gouv.xaf.backweb.formbean.MotifsFormBean;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller pour les fonctionnalites (onglets) Utilisateurs et Paramtres
 *
 * @author tverdoyan
 */
@Controller
@Secured({"ROLE_PARAMETRAGE", "ROLE_CONFIGURATION"})
@RequestMapping("/gestion/parametres")
public class GestionParametresController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametresController.class);
    private static final String ERROR_LIST = "errorList";
    private static final String PARAMETRE_NEW_URL = "gestion/parametres/parametresNew";
    private static final String STATUT_PARAM = "statuts";
    private static final String ERR_CONTACT = "Un problème technique a été rencontré veuillez contacter la Direction Informatique.";
    private static final String REDIRECT_GESTION_PARAMETRES = "redirect:/gestion/parametres";

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MotifsService motifsService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @GetMapping
    public ModelAndView form(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean) {
        LOGGER.info("Appel de la page /gestion/parametres. Méthode form");
        String errorList = null;
        ModelAndView mav = new ModelAndView("gestion/parametres/parametres");
        try {
            List<MotifDTO> list = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
            List<CustomMotifDTO> customlist = regroupeLibelle(list);
            if (customlist.isEmpty()) {
                errorList = ERR_CONTACT;
            }
            mav.addObject("motifs", customlist);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans form (/)");
            throw new DemarchesServiceException("Exception rencontrée dans GestionParametresController.form()", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        mav.addObject(ERROR_LIST, errorList);
        LOGGER.info("======================= Fin /gestion/parametres. Méthode form");
        return mav;
    }

    @GetMapping(value = "/newInit")
    public ModelAndView formInit(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean) {
        LOGGER.info("Appel de la page /gestion/parametres/newInit. Méthode formInit");
        ModelAndView mav = new ModelAndView(PARAMETRE_NEW_URL);
        try {
            // Liste des enum actuellement utilisés
            List<GenericStatusDTO> list = demarchesDataProvider.getCandidateStatusesForMotifs();
            mav.addObject(STATUT_PARAM, list);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formInit (/newInit)");
            throw new DemarchesServiceException("Exception rencontrée dans formInit (/newInit)", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        LOGGER.info("======================= Fin /gestion/parametres/newInit. Méthode formInit");
        return mav;
    }

    /**
     * Activation du motif (mise de la date du jour)
     *
     * @param motifsFormBean le formulaire de la page des motifs
     */
    private ModelAndView activationMotif(MotifsFormBean motifsFormBean) {
        LOGGER.info("Activation du motif de code : {}", motifsFormBean.getCodeVisible());
        String code = motifsFormBean.getCode();
        boolean isMotifFound = false;
        if (StringUtils.isNotBlank(code)) {
            List<MotifDTO> listMotif = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
            for (MotifDTO motif : listMotif) {
                if (motif.getCode().equals(code)) {
                    motif.setDateArchive(null);
                    motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motif);
                    isMotifFound = true;
                }
            }
        }
        try {
            if (!isMotifFound) {
                // Code motif non exploitable ou non existant
                motifsFormBean.setIsErrGlobale(true);
            } else {
                motifsCache.refresh();
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans activationMotif");
            throw new DemarchesServiceException("Exception rencontrée dans activationMotif", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("sortie méthode activationMotif");
        return new ModelAndView(REDIRECT_GESTION_PARAMETRES);
    }

    /**
     * Desactivation du motif (mise de la date du jour) *
     *
     * @param motifsFormBean le formulaire de la page des motifs
     */
    private ModelAndView desactivationMotif(MotifsFormBean motifsFormBean) {
        LOGGER.info("Desactivation du motif de code : {}", motifsFormBean.getCodeVisible());
        String code = motifsFormBean.getCode();
        boolean isMotifFound = false;
        if ((StringUtils.isNotBlank(code))) {
            // Liste des motifs
            List<MotifDTO> listMotif = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
            for (MotifDTO motif : listMotif) {
                if (motif.getCode().equals(code)) {
                    motifsService.deleteMotif(gouvPropertiesResolver.getDemarcheId(), motif.getPkMotifs());
                    isMotifFound = true;
                }
            }
        }
        try {
            if (!isMotifFound) {
                // Code motif non exploitable ou non existant
                motifsFormBean.setIsErrGlobale(true);
            } else {
                motifsCache.refresh();
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans desactivationMotif");
            throw new DemarchesServiceException("Exception rencontrée dans desactivationMotif", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("sortie méthode desactivationMotif");
        return new ModelAndView(REDIRECT_GESTION_PARAMETRES);
    }

    @PostMapping(value = "/newCreate")
    public ModelAndView formCreate(@Valid @ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean,
                                   BindingResult results, @RequestParam(required = false) String desactiveMotif,
                                   @RequestParam(required = false) String activeMotif) {

        LOGGER.info("Appel de la page /gestion/parametres/newCreate. Méthode formCreate");

        // Recupere le code hidden (codeVisible), si renseigné
        if (StringUtils.isBlank(motifsFormBean.getCode()) && StringUtils.isNotBlank(motifsFormBean.getCodeVisible())) {
            motifsFormBean.setCode(motifsFormBean.getCodeVisible());
        }
        // Unset le code visible, pour éviter de le reprendre si code principal
        // renseigné
        motifsFormBean.setCodeVisible(null);

        if (StringUtils.isNotBlank(desactiveMotif)) {
            return desactivationMotif(motifsFormBean);
        }

        if (StringUtils.isNotBlank(activeMotif)) {
            return activationMotif(motifsFormBean);
        }

        if (!(motifsFormBean.getIsErrGlobale() || results.hasErrors())) {
            return new ModelAndView(REDIRECT_GESTION_PARAMETRES);
        }

        String errorList = getErrorList(motifsFormBean);
        ModelAndView mav = new ModelAndView(PARAMETRE_NEW_URL);

        // Liste des enum actuellement utilisés
        List<GenericStatusDTO> list = demarchesDataProvider.getCandidateStatusesForMotifs();
        mav.addObject(STATUT_PARAM, list);
        mav.addObject(ERROR_LIST, errorList);

        LOGGER.info("======================= Fin /gestion/parametres/newCreate. Méthode formCreate");

        return mav;
    }

    private String getErrorList(MotifsFormBean motifsFormBean) {
        String errorList = "";
        if (motifsFormBean.getMotifPkFr() != null && motifsFormBean.getMotifPkFr() > 0) {
            errorList = miseAjourMotif(motifsFormBean);
        } else {
            if (StringUtils.isNotBlank(motifsFormBean.getCode())
                    && StringUtils.isNotBlank(motifsFormBean.getLibelleFr())
                    && motifsFormBean.getStatutEnum() != null) {
                // Saisie des donnees
                List<MotifDTO> localAllMotifs = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
                String code = StringUtils.stripAccents(motifsFormBean.getCode().replace(" ", "_").toUpperCase());
                if (checkCodeExistence(localAllMotifs, code)) {
                    motifsFormBean.setIsErrCodeExiste(true);
                    motifsFormBean.setIsErrGlobale(true);
                } else {
                    errorList = createMotif(motifsFormBean, code);
                }
            } else {
                errorList = "Mise à jour impossible. Données isuffisantes pour le motif de code : " + motifsFormBean.getCode();
                LOGGER.error(errorList);
            }
        }
        return errorList;
    }

    /**
     * Mise à jour d'un motif
     */
    private String miseAjourMotif(MotifsFormBean motifsFormBean) {
        LOGGER.info("Méthode formCreate --> update");
        Integer pkMotFr = motifsFormBean.getMotifPkFr();
        String errorList = "";
        if (pkMotFr == null || pkMotFr <= 0) {
            motifsFormBean.setIsErrGlobale(true);
            errorList = "Motif(s) non identifié(s)";
        } else {
            MotifDTO motif;
            String demarcheId = gouvPropertiesResolver.getDemarcheId();
            if (motifsFormBean.getMotifPkFr() != null) {
                motif = motifsService.getMotif(demarcheId, motifsFormBean.getMotifPkFr());
                motif.setCode(motifsFormBean.getCode());
                motif.setStatut(motifsFormBean.getStatutEnum());
                motif.setLibelle(motifsFormBean.getLibelleFr());
                motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliFr());
                motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerFr());
                motifsService.saveOrUpdateMotif(demarcheId, motif);
            }
            if (motifsFormBean.getMotifPkEn() != null) {
                motif = motifsService.getMotif(demarcheId, motifsFormBean.getMotifPkEn());
                motif.setCode(motifsFormBean.getCode());
                motif.setStatut(motifsFormBean.getStatutEnum());
                if (StringUtils.isNotBlank(motifsFormBean.getLibelleEn())) {
                    motif.setLibelle(motifsFormBean.getLibelleEn());
                    motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliEn());
                    motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerEn());
                } else {
                    motif.setLibelle(motifsFormBean.getLibelleFr());
                    motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliFr());
                    motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerFr());
                }
                motifsService.saveOrUpdateMotif(demarcheId, motif);
            }
        }
        // MAJ du cache
        motifsCache.refresh();
        return errorList;
    }

    private String createMotif(MotifsFormBean motifsFormBean, String code) {
        LOGGER.info("Méthode formCreate --> create");
        String errorList = "";
        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        // Donnees communes
        MotifDTO motif = new MotifDTO();
        motif.setCode(code);

        String statEnum = motifsFormBean.getStatutEnum();
        if (statEnum == null) {
            errorList = "Un problème technique a été rencontré. " + ERR_CONTACT;
        } else {
            motif.setStatut(statEnum);
        }
        motif.setDemarcheId(demarcheId);

        // Donnees specifiques et insert (français)
        motif.setLibelle(motifsFormBean.getLibelleFr());
        motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliFr());
        motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerFr());
        motif.setLangue("fr");
        motifsService.saveOrUpdateMotif(demarcheId, motif);

        // Donnees specifiques et insert (anglais)
        if (StringUtils.isNotBlank(motifsFormBean.getLibelleEn())) {
            motif.setLibelle(motifsFormBean.getLibelleEn());
            motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliEn());
            motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerEn());
        }
        // Si les champs ne sont pas renseigné, on insère les données FR
        motif.setLangue("en");
        motifsService.saveOrUpdateMotif(demarcheId, motif);

        // MAJ du cache
        motifsCache.refresh();
        return errorList;
    }

    /**
     * Renvoie true si le code du motif a creer existe déjà
     */
    private boolean checkCodeExistence(List<MotifDTO> localAllMotifs, String code) {
        for (MotifDTO motif : localAllMotifs) {
            if (motif.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping(path = "/updateInit")
    public ModelAndView formUpdateInit(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean) {
        LOGGER.info("Appel de la page /gestion/parametres/updateInit. Méthode formUpdateInit");
        String errorList = "";
        ModelAndView mav;
        try {
            mav = new ModelAndView(PARAMETRE_NEW_URL);
            if (StringUtils.isNotBlank(motifsFormBean.getCode())) {
                // Donnees communes
                List<MotifDTO> listMotif = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
                // Donnees specifiques et insert (français)
                for (MotifDTO motif : listMotif) {
                    if (motif.getCode().equals(motifsFormBean.getCode())) {
                        motifsFormBean.setCode(motif.getCode());
                        if ("fr".equalsIgnoreCase(motif.getLangue())) {
                            motifsFormBean.setLibelleFr(motif.getLibelle());
                            motifsFormBean.setCommentairePrerempliFr(motif.getCommentairePrerempli());
                            motifsFormBean.setMotifPkFr(motif.getPkMotifs());
                        } else {
                            motifsFormBean.setLibelleEn(motif.getLibelle());
                            motifsFormBean.setCommentairePrerempliEn(motif.getCommentairePrerempli());
                            motifsFormBean.setMotifPkEn(motif.getPkMotifs());
                        }
                        motifsFormBean.setDateArchive(getDateArchiveStr(motif));
                        motifsFormBean.setStatutEnum(motif.getStatut());
                        motifsFormBean.setStatut(motif.getStatut());
                        motifsFormBean.setHashCode(motif.getStatut().hashCode());
                    }
                }
            } else {
                motifsFormBean.setIsErrGlobale(true);
                errorList = "Motif non identifié";
            }

            // Liste des enum actuellement utilisés
            mav.addObject(STATUT_PARAM, getListEnumsContainsMotifs());
            if (errorList.length() > 0) {
                mav.addObject(ERROR_LIST, errorList);
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formUpdateInit.");
            throw new DemarchesServiceException("Exception rencontrée dans formUpdateInit.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("======================= Fin /gestion/parametres/updateInit. Méthode formUpdateInit");
        return mav;
    }

    /**
     * Liste des enums actuellement utilisés (au 02/11/2016)
     */
    private List<GenericStatusDTO> getListEnumsContainsMotifs() {
        return demarchesDataProvider.getCandidateStatusesForMotifs();
    }

    private String getDateArchiveStr(MotifDTO motif) {
        try {
            if (motif.getDateArchive() != null) {
                Locale.setDefault(Locale.FRANCE);
                String frm = "dd/MM/yyyy";
                SimpleDateFormat sf = new SimpleDateFormat(frm);
                return sf.format(motif.getDateArchive());
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans getDateArchiveStr");
            throw new DemarchesServiceException("Exception rencontrée dans getDateArchiveStr", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "";
    }

    /**
     * Regroupement des libellés fr et en
     */
    private List<CustomMotifDTO> regroupeLibelle(List<MotifDTO> list) {
        List<CustomMotifDTO> listFinale;
        try {
            listFinale = new ArrayList<>();
            Map<String, CustomMotifDTO> map = new HashMap<>();
            CustomMotifDTO customMotifDTO;

            // Regroupe Les libellés
            for (MotifDTO m : list) {
                if (!map.containsKey(m.getCode())) {
                    customMotifDTO = new CustomMotifDTO();
                    customMotifDTO.setCode(m.getCode());
                    customMotifDTO.setDateArchive(m.getDateArchive());
                    customMotifDTO.setDemarcheId(m.getDemarcheId());
                    customMotifDTO.setLangue(m.getLangue());
                    customMotifDTO.setPkMotifs(m.getPkMotifs());
                    customMotifDTO.setStatut(m.getStatut());
                    map.put(m.getCode(), customMotifDTO);
                } else {
                    customMotifDTO = map.get(m.getCode());
                }
                if ("fr".equalsIgnoreCase(m.getLangue())) {
                    customMotifDTO.setLibelleFr(m.getLibelle());
                    customMotifDTO.setCommentairePrerempliFr(m.getCommentairePrerempli());
                } else {
                    customMotifDTO.setLibelleEn(m.getLibelle());
                    customMotifDTO.setCommentairePrerempliEn(m.getCommentairePrerempli());
                }
            }
            for (Map.Entry<String, CustomMotifDTO> entry : map.entrySet()) {
                listFinale.add(entry.getValue());
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans regroupeLibelle");
            throw new DemarchesServiceException("Exception rencontrée dans regroupeLibelle", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return listFinale;
    }
}
