package mc.gouv.xaf.rio.service.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.rio.dto.*;
import mc.gouv.xaf.rio.enums.ArchivageStatutAvancementEnum;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.rio.service.ConvertisseurTiffService;
import mc.gouv.xaf.rio.service.RioService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ArchivageServiceImpl implements ArchivageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivageServiceImpl.class);

    private final SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final String STATUT_OK = "Succès";
    private final String STATUT_KO = "Échec";

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MailService mailService;

    @Autowired
    private ConvertisseurTiffService convertisseurTiffService;

    @Autowired
    private RioService rioService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesFilesService demandesFilesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Transactional
    public List<DemandeFileDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files, DemandeDTO demandeDTO) {

        LOGGER.info("Début archivage des documents");
        List<DemandeFileDTO> fileDocumentList = new ArrayList<>();
        double progresArchivage = 0;
        double valeurStep = 1d / files.size();
        int demandeId = demandeDTO.getPkDemandes();
        boolean erreurRIO = false;
        boolean erreurConvertisseur = false;
        ArchivageStatutDTO archivageStatut = new ArchivageStatutDTO();
        archivageStatut.setAvancement(ArchivageStatutAvancementEnum.EN_COURS);
        archivageStatut.setProgression(progresArchivage);
        archivageProgress.put(demandeId, archivageStatut);

        ArchivageRapportExportDTO archivageRapportExportDTO = new ArchivageRapportExportDTO();

        // En début de process, on boucle sur les fichiers pour initialiser le rapport d'archivage
        // Si on le fait après, il pourrait y avoir une erreur qui stop le process
        for (DemandeFileDTO file : files) {
            archivageRapportExportDTO.addFichiersInitiaux(createFichierInitial(file));
        }

        LOGGER.info("Vérification de l'existence du document");
        RioDocumentDTO rioDocumentDTO = new RioDocumentDTO();
        try {
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
            erreurRIO = true;
        }

        for (DemandeFileDTO file : files) {
            Map<String, InputStream> filesTiff;
            try {
                LOGGER.info("Génération des images TIFFs pour fichier {}", file.getName());
                filesTiff = convertisseurTiffService.generateTiffs(file);

                // Ajout dans la liste des fichiers qui ont bien été convertis
                for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                    archivageRapportExportDTO.addFichiersConvertis(createFichierConverti(file, STATUT_OK, fileTiff.getKey()));
                }
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la conversion du document {}", file.getName(), e);
                archivageRapportExportDTO.addFichiersConvertis(createFichierConverti(file, STATUT_KO, ""));
                archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_KO, ""));

                // On fait avancer les steps d'archivage
                progresArchivage += valeurStep;
                archivageStatut.setProgression(progresArchivage);

                erreurConvertisseur = true;
                archivageStatut.setNbFichiersEnErreur(archivageStatut.getNbFichiersEnErreur()+1);

                continue;
            }

            boolean erreurArchivageFichierCourrant = false;
            for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                try {
                    LOGGER.info("Envoi du documents en GED pour {}", fileTiff.getKey());
                    rioService.createFileDocument(rioDocumentDTO.getRefDocument(), fileTiff.getKey(), IOUtils.toByteArray(fileTiff.getValue()));
                    fileTiff.getValue().close();
                    archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_OK, fileTiff.getKey()));
                } catch (Exception e) {
                    LOGGER.error("Erreur lors de l'archivage du document {}", file.getName(), e);
                    archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_KO, fileTiff.getKey()));
                    erreurRIO = true;
                    erreurArchivageFichierCourrant = true;
                }
            }

            // On fait avancer le step de l'archivage au prochain fichier
            progresArchivage += valeurStep;
            archivageStatut.setProgression(progresArchivage);

            // On marque le document comme "archivé" si et seulement si toutes ses pages (docs tiff) ont été archivés
            if (erreurArchivageFichierCourrant) {
                archivageStatut.setNbFichiersEnErreur(archivageStatut.getNbFichiersEnErreur()+1);
            } else {
                fileDocumentList.add(file);
            }
        }

        archivageStatut.setProgression(1d);
        archivageStatut.setAvancement(ArchivageStatutAvancementEnum.COMPLETE);

        LOGGER.info("Fin archivage des documents");

        archivageRapportExportDTO.setDemarcheId(demandeDTO.getDemarcheId());
        archivageRapportExportDTO.setDemandeFlatDTO(afBackUtils.demandeDTOToDemandeFlatDTO(demandeDTO));

        try (ByteArrayOutputStream rapport = generateArchivageRecap(archivageRapportExportDTO, demandeDTO)) {
            processErreursArchivage(erreurRIO, erreurConvertisseur, demandeDTO, rapport);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la génération du rapport d'archivage pour la demande {}", demandeId, e);
        }

        return fileDocumentList;
    }

    private void processErreursArchivage(boolean erreurRIO, boolean erreurConvertisseur, DemandeDTO demandeDTO, ByteArrayOutputStream rapport) {
        if (erreurRIO) {
            sendMailProblemeRIO(demandeDTO, rapport);
        } else if (erreurConvertisseur) {
            sendMailProblemeConvertisseur(demandeDTO, rapport);
        }
    }

    private void sendMailProblemeConvertisseur(DemandeDTO demandeDTO, ByteArrayOutputStream rapport) {
        String subjectTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_CORPS";
        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, InputStream> pj = new HashMap<>();
        pj.put("Rapport archivage.xlsx", new ByteArrayInputStream(rapport.toByteArray()));
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), 8, null, pj);
    }

    private void sendMailProblemeRIO(DemandeDTO demandeDTO, ByteArrayOutputStream rapport) {
        String subjectTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_OBJET";
        String bodyTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_CORPS";
        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, InputStream> pj = new HashMap<>();
        pj.put("Rapport archivage.xlsx", new ByteArrayInputStream(rapport.toByteArray()));
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), 9, null, pj);
    }

    private ByteArrayOutputStream generateArchivageRecap(ArchivageRapportExportDTO rapportExportDTO, DemandeDTO demandeDTO) throws Exception {
        LOGGER.info("Constitution du modèle pour la génération du recap archivage...");
        Map<String, Object> model = new HashMap<>();
        model.put("demarcheId", rapportExportDTO.getDemarcheId());
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        model.put("dateGeneration", dateTimeString);
        model.put("demande", rapportExportDTO.getDemandeFlatDTO());
        model.put("fichiersInitiaux", rapportExportDTO.getFichiersInitiaux());
        model.put("fichiersConvertis", rapportExportDTO.getFichiersConvertis());
        model.put("fichiersDeposes", rapportExportDTO.getFichiersDeposes());

        LOGGER.info("Génération du fichier...");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        excelExportService.exportExcel("rapport_archivage.xlsx", model, output);

        LOGGER.info("Sauvegarde du fichier...");

        String fileName = "Export_Archivage_" + demandeDTO.getIdentifiant() + "_" + AfBackUtils.generateFileDateSuffix() + ".xlsx";
        ByteArrayOutputStream outputSave = new ByteArrayOutputStream();
        String url = fileService.saveFile(demandeDTO, fileName, gouvPropertiesResolver.getContainerId(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(output.toByteArray()), outputSave);

        outputSave.close();

        saveFichier(fileName, url, demandeDTO, demandeDTO.getDemarcheId());

        return output;
    }

    private void saveFichier(String fileName, String url, DemandeDTO demande, String demarcheId) {
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(fileName);
        file.setUrl('/' + url);
        file.setDate(new Date());
        file.setMeta("BACK_RAPPORT_ARCHIVAGE");
        demandesFilesService.saveFile(file, demarcheId, demande.getPkDemandes(), false);
    }

    private ArchivageFichierInitalDTO createFichierInitial(DemandeFileDTO file) {
        ArchivageFichierInitalDTO fichierInitalDTO = new ArchivageFichierInitalDTO();
        String filename = file.getName().substring(0, file.getName().lastIndexOf("."));
        String extension = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
        fichierInitalDTO.setNom(filename);
        fichierInitalDTO.setFormat(extension);
        return fichierInitalDTO;
    }

    private ArchivageFichierConvertiDTO createFichierConverti(DemandeFileDTO file, String statut, String nomTiff) {
        ArchivageFichierConvertiDTO fichierConvertiDTO = new ArchivageFichierConvertiDTO();
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        fichierConvertiDTO.setNom(file.getName());
        fichierConvertiDTO.setNomTiff(nomTiff);
        fichierConvertiDTO.setStatut(statut);
        fichierConvertiDTO.setDateConversion(dateTimeString);
        return fichierConvertiDTO;
    }

    private ArchivageFichierDeposeDTO createFichierDepose(DemandeFileDTO file, String statut, String nomTiff) {
        ArchivageFichierDeposeDTO fichierDeposeDTO = new ArchivageFichierDeposeDTO();
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        fichierDeposeDTO.setNom(file.getName());
        fichierDeposeDTO.setNomTiff(nomTiff);
        fichierDeposeDTO.setStatut(statut);
        fichierDeposeDTO.setDate(dateTimeString);
        return fichierDeposeDTO;
    }
}
