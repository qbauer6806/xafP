package mc.gouv.xaf.backweb.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.shared.dto.GenericStatusDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.backweb.dto.CustomMotifDTO;
import mc.gouv.xaf.backweb.formbean.MotifsFormBean;

/**
 * Controller pour les fonctionnalites (onglets) Utilisateurs et Paramtres
 * 
 * @author tverdoyan
 * 
 */
@Controller
@Secured("ROLE_PARAMETRAGE")
@RequestMapping("/gestion/parametres")
public class GestionParametresController extends AbstractController {

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MotifsService motifsService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionParametresController.class);

    private String errContact = "Un problème technique a été rencontré veuillez contacter la Direction Informatique.";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean) throws Exception {

        LOGGER.info("Appel de la page /gestion/parametres. Méthode form");
        String errorList = null;
        ModelAndView mav = new ModelAndView("gestion/parametres/parametres");

        try {
            List<MotifDTO> list = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());

            List<CustomMotifDTO> customlist = regroupeLibelle(list);
            if (customlist == null) {
                errorList = errContact;
            }

            mav.addObject("motifs", customlist);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans form (/)", e);
            throw new Exception(e);
        }

        mav.addObject("errorList", errorList);

        LOGGER.info("======================= Fin /gestion/parametres. Méthode form");

        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/newInit")
    public ModelAndView formInit(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean) throws Exception {

        LOGGER.info("Appel de la page /gestion/parametres/newInit. Méthode formInit");

        ModelAndView mav = new ModelAndView("gestion/parametres/parametresNew");

        try {
            // Liste des enum actuellement utilisés
            List<GenericStatusDTO> list = demarchesDataProvider.getCandidateStatusesForMotifs();
            mav.addObject("statuts", list);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formInit (/newInit)", e);
            throw new Exception(e);
        }

        LOGGER.info("======================= Fin /gestion/parametres/newInit. Méthode formInit");

        return mav;
    }

    /**
     * Activation du motif (mise de la date du jour)
     * 
     * @param code
     *            code du motif devant être activer (date mise à null)
     */
    private boolean activationMotif(String code) throws Exception {

        LOGGER.info("Appel de la fonction activationMotif");
        boolean isMotifFound = false;
        boolean isCodeMotif = false;

        if (StringUtils.isNotBlank(code)) {
            isCodeMotif = true;
            List<MotifDTO> listMotif = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
            for (MotifDTO motif : listMotif) {
                if (motif.getCode().equals(code)) {
                    motif.setDateArchive(null);
                    motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motif);

                    isMotifFound = true;
                }
            }
        }

        return isMotifFound && isCodeMotif;
    }

    /**
     * Desactivation du motif (mise de la date du jour) * @param le code du motif devant être desactiver (date à
     * renseigner)
     */
    private boolean desactivationMotif(String code) throws Exception {

        LOGGER.info("Appel de la fonction desactivationMotif");
        boolean isMotifFound = false;
        boolean isCodeMotif = false;

        if ((StringUtils.isNotBlank(code))) {
            isCodeMotif = true;

            // Liste des motifs
            List<MotifDTO> listMotif = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
            for (MotifDTO motif : listMotif) {
                if (motif.getCode().equals(code)) {
                    motifsService.deleteMotif(gouvPropertiesResolver.getDemarcheId(), motif.getPkMotifs());
                    isMotifFound = true;
                }
            }
        }

        return (isMotifFound && isCodeMotif);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/newCreate")
    public ModelAndView formCreate(@Valid @ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean,
            BindingResult results, @RequestParam(required = false) String desactiveMotif,
            @RequestParam(required = false) String activeMotif) throws Exception {

        LOGGER.info("Appel de la page /gestion/parametres/newCreate. Méthode formCreate");
        boolean bIsErrDetec = false;

        // Recupere le code hidden (codeVisible), si renseigné
        if (StringUtils.isBlank(motifsFormBean.getCode()) && StringUtils.isNotBlank(motifsFormBean.getCodeVisible())) {
            motifsFormBean.setCode(motifsFormBean.getCodeVisible());
        }
        // Unset le code visible, pour éviter de le reprendre si code principal
        // renseigné
        motifsFormBean.setCodeVisible(null);

        if (StringUtils.isNotBlank(desactiveMotif)) {
            LOGGER.info("Desactivation du motif de code : " + motifsFormBean.getCodeVisible());

            try {
                if (desactivationMotif(motifsFormBean.getCode()) == false) {
                    // Code motif non exploitable ou non existant
                    motifsFormBean.setIsErrGlobale(true);
                } else {
                    motifsCache.refresh();
                }
            } catch (Exception e) {
                LOGGER.error("Exception rencontrée dans desactivationMotif", e);
                throw new Exception(e);
            }

            LOGGER.info("sortie méthode desactivationMotif");
            ModelAndView mav = new ModelAndView("redirect:/gestion/parametres");
            return mav;
        } else if (StringUtils.isNotBlank(activeMotif)) {
            LOGGER.info("Activation du motif de code : " + motifsFormBean.getCodeVisible());

            try {
                if (activationMotif(motifsFormBean.getCode()) == false) {
                    // Code motif non exploitable ou non existant
                    motifsFormBean.setIsErrGlobale(true);
                } else {
                    motifsCache.refresh();
                }
            } catch (Exception e) {
                LOGGER.error("Exception rencontrée dans activationMotif", e);
                throw new Exception(e);
            }

            LOGGER.info("sortie méthode activationMotif");
            ModelAndView mav = new ModelAndView("redirect:/gestion/parametres");
            return mav;
        } else {
            ModelAndView mav;
            String errorList = "";
            boolean bIsUpd = false;

            if (results.hasErrors()) {
                bIsErrDetec = true;
            } else {
                bIsUpd = motifsFormBean.getMotifPkFr() != null && motifsFormBean.getMotifPkFr() > 0;
                if (bIsUpd) {
                    LOGGER.info("Méthode formCreate --> update");
                    try {

                        Integer pkMotFr = motifsFormBean.getMotifPkFr();

                        if ((pkMotFr == null || (pkMotFr != null && pkMotFr <= 0))) {
                            motifsFormBean.setIsErrGlobale(true);
                            errorList = "Motif(s) non identifié(s)";
                        } else {
                            miseAjourMotif(motifsFormBean);
                        }

                        // MAJ du cache
                        motifsCache.refresh();

                    } catch (Exception e) {
                        LOGGER.error("Exception rencontrée dans formCreate", e);
                        errorList = errContact;
                        throw new Exception(e);
                    }
                } else {

                    LOGGER.info("Méthode formCreate --> create");

                    try {

                        boolean bIsCodeOK = StringUtils.isNotBlank(motifsFormBean.getCode());
                        boolean bIsLibFrOK = StringUtils.isNotBlank(motifsFormBean.getLibelleFr());

                        if (bIsCodeOK && bIsLibFrOK && motifsFormBean.getStatutEnum() != null) {
                            // Saisie des donnees

                            List<MotifDTO> localAllMotifs = motifsService
                                    .getMotifs(gouvPropertiesResolver.getDemarcheId());

                            boolean bIsMotifCodeExists = checkCodeExistence(localAllMotifs,
                                    motifsFormBean.getCode().replaceAll(" ", "_").toUpperCase());

                            if (!bIsMotifCodeExists) {
                                // Donnees communes
                                MotifDTO motif = new MotifDTO();
                                motif.setCode(motifsFormBean.getCode().replaceAll(" ", "_").toUpperCase());
                                motif.setCode(StringUtils.stripAccents(motif.getCode()));

                                String statEnum = motifsFormBean.getStatutEnum();
                                if (statEnum == null) {
                                    errorList = "Un problème technique a été rencontré. " + errContact;
                                } else {
                                    motif.setStatut(statEnum);
                                }

                                motif.setDemarcheId(gouvPropertiesResolver.getDemarcheId().trim());

                                // Donnees specifiques et insert (français)
                                motif.setLibelle(motifsFormBean.getLibelleFr());
                                motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliFr());
                                motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerFr());
                                motif.setLangue("fr");
                                motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motif);

                                // Donnees specifiques et insert (anglais)
                                if (!StringUtils.isEmpty(motifsFormBean.getLibelleEn())) {
                                    motif.setLibelle(motifsFormBean.getLibelleEn());
                                    motif.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliEn());
                                    motif.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerEn());
                                }
                                // Si les champs ne sont pas renseigné, on insère les données FR
                                motif.setLangue("en");
                                motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motif);

                                // MAJ du cache
                                motifsCache.refresh();

                            } else {
                                motifsFormBean.setIsErrCodeExiste(true);
                                motifsFormBean.setIsErrGlobale(true);
                            }
                        } else {
                            LOGGER.error("Mise à jour impossible. Données isuffisantes pour le motif de code : "
                                    + motifsFormBean.getCode());
                            errorList = "Mise à jour impossible. Données isuffisantes pour le motif de code : "
                                    + motifsFormBean.getCode();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Exception rencontrée dans formCreate", e);
                        errorList = "Un problème technique a été rencontrée";
                        throw new Exception(e);
                    }
                }
            }

            if (motifsFormBean.getIsErrGlobale() || bIsErrDetec) {
                mav = new ModelAndView("gestion/parametres/parametresNew");

                // Liste des enum actuellement utilisés
                List<GenericStatusDTO> list = demarchesDataProvider.getCandidateStatusesForMotifs();
                mav.addObject("statuts", list);

                mav.addObject("errorList", errorList);
            } else {
                mav = new ModelAndView("redirect:/gestion/parametres");
            }

            LOGGER.info("======================= Fin /gestion/parametres/newCreate. Méthode formCreate");

            return mav;

        }
    }

    /**
     * Mise à jour d'un motif
     */
    private void miseAjourMotif(MotifsFormBean motifsFormBean) throws Exception {
        if (motifsFormBean.getMotifPkFr() != null) {
            MotifDTO motifFr = motifsService.getMotif(gouvPropertiesResolver.getDemarcheId(),
                    motifsFormBean.getMotifPkFr());
            motifFr.setCode(motifsFormBean.getCode());
            motifFr.setStatut(motifsFormBean.getStatutEnum());
            motifFr.setLibelle(motifsFormBean.getLibelleFr());
            motifFr.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliFr());
            motifFr.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerFr());
            motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motifFr);
        }

        if (motifsFormBean.getMotifPkEn() != null) {
            MotifDTO motifEn = motifsService.getMotif(gouvPropertiesResolver.getDemarcheId(),
                    motifsFormBean.getMotifPkEn());
            motifEn.setCode(motifsFormBean.getCode());
            motifEn.setStatut(motifsFormBean.getStatutEnum());
            motifEn.setLibelle(motifsFormBean.getLibelleEn());
            motifEn.setCommentairePrerempli(motifsFormBean.getCommentairePrerempliEn());
            motifEn.setTexteAEnvoyer(motifsFormBean.getTexteAEnvoyerEn());
            motifsService.saveOrUpdateMotif(gouvPropertiesResolver.getDemarcheId(), motifEn);
        }
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

    @RequestMapping(method = RequestMethod.GET, path = "/updateInit")
    public ModelAndView formUpdateInit(@ModelAttribute("motifsFormBean") MotifsFormBean motifsFormBean)
            throws Exception {

        LOGGER.info("Appel de la page /gestion/parametres/updateInit. Méthode formUpdateInit");
        String errorList = "";
        ModelAndView mav = null;

        try {
            mav = new ModelAndView("gestion/parametres/parametresNew");

            if (StringUtils.isNotBlank(motifsFormBean.getCode())) {
                // Saisie des donnees

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
            mav.addObject("statuts", getListEnumsContainsMotifs());

            if (errorList.length() > 0) {
                mav.addObject("errorList", errorList);
            }

        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formUpdateInit. Msg : " + e);
            throw new Exception(e);
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

    private String getDateArchiveStr(MotifDTO motif) throws Exception {
        try {

            if (motif.getDateArchive() != null) {
                Locale.setDefault(Locale.FRANCE);
                String frm = "dd/MM/yyyy";
                SimpleDateFormat sf = new SimpleDateFormat(frm);
                return sf.format(motif.getDateArchive());
            }
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans getDateArchiveStr", e);
            throw new Exception(e);
        }

        return "";
    }

    /**
     * Regroupement des libellés fr et en
     */
    List<CustomMotifDTO> regroupeLibelle(List<MotifDTO> list) throws Exception {

        List<CustomMotifDTO> listFinale = null;
        try {
            listFinale = new ArrayList<CustomMotifDTO>();
            Map<String, CustomMotifDTO> map = new HashMap<String, CustomMotifDTO>();
            CustomMotifDTO customMotifDTO = null;

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
            LOGGER.error("Exception rencontrée dans regroupeLibelle", e);
            throw new Exception(e);
        }

        return listFinale;
    }

}
