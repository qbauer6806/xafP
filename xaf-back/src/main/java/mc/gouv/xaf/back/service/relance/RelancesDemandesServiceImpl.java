package mc.gouv.xaf.back.service.relance;

import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class RelancesDemandesServiceImpl implements RelancesDemandesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelancesDemandesServiceImpl.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private RelancesUtils relanceUtils;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Override
    public void sendRelancesMail(List<RelanceStatutDemandeConf> statutsARelancer) {
        // On recupère toutes les demandes du TS appelant qui feront l'objet d'une relance
        Map<DemandeDTO, String> demandesANotifier = relanceUtils.getDemandesANotifier(statutsARelancer);
        for (Map.Entry<DemandeDTO, String> entry : demandesANotifier.entrySet()) {
            LOGGER.info("Début du processus de relance des demandes...");
            envoiEmailUsagerRelance(entry.getKey(), entry.getValue());
            relanceUtils.setRelanceDate(entry.getKey());
        }
    }

    @Override
    public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix) {
        final String subjectTemplateCode = codeMailPrefix + "_OBJET";
        final String bodyTemplateCode = codeMailPrefix + "_CORPS";

        EmailInfoDTO emailInfoDTO = relanceUtils.creationMailUsager(bodyTemplateCode, subjectTemplateCode,
                demande.getLangue());
        DemandeUsagerDTO usager = demande.getUsager();
        if (usager != null) {
            emailInfoDTO.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        }

        Map<String, Object> model = afMailTemplateModelProvider.getGenericModelDemande(demande);
        model.put("expireDans", relanceUtils.getExpirationTime(demande));

        try {
            mailService.sendMail(emailInfoDTO, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email usager de relance pour la demande {}",
                    demande.getIdentifiant());
        }
    }
}
