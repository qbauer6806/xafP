package mc.gouv.xaf.back.service.itg.rio.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.ConvertisseurTiffService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rio.ArchivageService;
import mc.gouv.xaf.back.service.itg.rio.RioService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.itg.rio.RioDocumentDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ArchivageServiceImpl implements ArchivageService {

    private static Logger LOGGER = LoggerFactory.getLogger(ArchivageServiceImpl.class);

    private SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MailService mailService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private ConvertisseurTiffService convertisseurTiffService;

    @Autowired
    private RioService rioService;


    @Transactional
    public List<DemandeFileDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files, int demandeId) {

        LOGGER.info("Début archivage des documents");
        List<DemandeFileDTO> fileDocumentList = new ArrayList<>();
        double progresArchivage = 0;
        double valeurStep = 1d / files.size();
        archivageProgress.put(demandeId, progresArchivage);

        LOGGER.info("Vérification de l'existence du document");
        RioDocumentDTO rioDocumentDTO = new RioDocumentDTO();
        try{
            // TODO To remove after testing
            PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_RIO");
            if (errorProp != null && "true".equals(errorProp.getValue()) ) {
                throw new Exception();
            }
            try {
                rioDocumentDTO = rioService.getDocument(refPermis);
            } catch (HttpServerErrorException e) {
                // Si le document n'existe pas, nous devons le créer
                if (e.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                    rioDocumentDTO = rioService.createDocument(refPermis);
                }
            }
        } catch (Exception e) {
            LOGGER.info("Problème avec l'api RIO");
            sendMailProblemeRIO(demandeId);
        }

        for (DemandeFileDTO file : files) {

            Map<String, InputStream> filesTiff;
            try {
                // TODO To remove after testing
                PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CONVERTISSEUR");
                if (errorProp != null && "true".equals(errorProp.getValue()) ) {
                    throw new Exception();
                }

                LOGGER.info("Génération des images TIFFs pour fichier {}", file.getName());
                filesTiff = convertisseurTiffService.generateTiffs(file);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la conversion du document {}", file.getName(), e);
                sendMailProblemeConvertisseur(demandeId);
                break;
            } finally {
                progresArchivage += valeurStep;
                archivageProgress.put(demandeId, progresArchivage);
            }

            try {
                for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                    LOGGER.info("Envoi du documents en GED pour {}", fileTiff.getKey());
                    rioService.createFileDocument(rioDocumentDTO.getRefDocument(), fileTiff.getKey(), IOUtils.toByteArray(fileTiff.getValue()));
                    fileTiff.getValue().close();
                }
                fileDocumentList.add(file);

            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'archivage du document {}", file.getName(), e);
                sendMailProblemeRIO(demandeId);
            } finally {
                progresArchivage += valeurStep;
                archivageProgress.put(demandeId, progresArchivage);
            }
        }

        archivageProgress.put(demandeId, 1d);

        LOGGER.info("Fin archivage des documents");

        return fileDocumentList;
    }

    private void sendMailProblemeConvertisseur(int demandeId) {
        String subjectTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_CORPS";
        List<String> list = getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        sendMailArchivage(subjectTemplateCode, bodyTemplateCode, demandeId, 8, list);
    }

    private void sendMailProblemeRIO(int demandeId) {
        String subjectTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_OBJET";
        String bodyTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_CORPS";
        List<String> list = getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        sendMailArchivage(subjectTemplateCode, bodyTemplateCode, demandeId, 9, list);
    }

    private void sendMailArchivage(String subjectTemplateCode, String bodyTemplateCode, int demandeId, int incident, List<String> mailingLists) {
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setLangue("fr");
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandeId + "");

        for(String mailingList : mailingLists) {
            String[] adresses = mailingList.trim().split(",");
            for (String adresseMail : adresses) {
                emailInfo.addTo(adresseMail, "Support Technique");
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("Pkdemandes", demandeId);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private List<String> getMailingLists(String... mailingListProps) {
        List<String> list = new ArrayList<>();
        for(String mailProp : mailingListProps) {
            PropertiesDTO mailProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), mailProp);
            if (mailProperty != null && StringUtils.isNotBlank(mailProperty.getValue())) {
                list.add(mailProperty.getValue());
            }
        }
        return list;
    }
}
