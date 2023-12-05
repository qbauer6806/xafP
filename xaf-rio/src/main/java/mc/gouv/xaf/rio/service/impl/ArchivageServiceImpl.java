package mc.gouv.xaf.rio.service.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.excel.ExcelExportService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.rio.dto.ArchivageFichierConvertiDTO;
import mc.gouv.xaf.rio.dto.ArchivageFichierDeposeDTO;
import mc.gouv.xaf.rio.dto.ArchivageFichierInitalDTO;
import mc.gouv.xaf.rio.dto.ArchivageRapportExportDTO;
import mc.gouv.xaf.rio.dto.ArchivageStatutDTO;
import mc.gouv.xaf.rio.dto.RioDocumentDTO;
import mc.gouv.xaf.rio.enums.ArchivageStatutAvancementEnum;
import mc.gouv.xaf.rio.service.ArchivageService;
import mc.gouv.xaf.rio.service.ConvertisseurTiffService;
import mc.gouv.xaf.rio.service.RioService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static mc.gouv.xaf.rio.utils.ArchivageUtils.ARCHIVAGE_RIO_COMPLETED;
import static mc.gouv.xaf.rio.utils.ArchivageUtils.CODE_TYPE_IMMAT;
import static mc.gouv.xaf.rio.utils.ArchivageUtils.CODE_TYPE_PERMIS;
import static mc.gouv.xaf.rio.utils.ArchivageUtils.NOMBRE_FICHIERS_ERREUR_ARCHIVAGE;

@Service
public class ArchivageServiceImpl implements ArchivageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivageServiceImpl.class);
    private final SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final String STATUT_OK = "Succès";
    private static final String STATUT_KO = "Échec";
    private static final String CODE_NOTICE_PERMIS = "CIR_PERMIS";
    private static final String CODE_NOTICE_REGISTRE = "CIR_CG";

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
    @Autowired
    private AfHistoService histoService;
    @Autowired
    private DemandesDataService demandesDataService;

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<String> archivagePermis(String refPermis, List<DemandeFileDTO> files, DemandeDTO demandeDTO) {
        LOGGER.info("Début archivage des documents dans le permis {}", refPermis);
        Map<String, String> references = new HashMap<>();
        references.put(refPermis, CODE_TYPE_PERMIS);
        return archiver(references, files, demandeDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<String> archivageRegistre(String refRegistre, List<DemandeFileDTO> files, DemandeDTO demandeDTO) {
        LOGGER.info("Début archivage des documents dans le registre {}", refRegistre);
        Map<String, String> references = new HashMap<>();
        references.put(refRegistre, CODE_TYPE_IMMAT);
        return archiver(references, files, demandeDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public List<String> archiver(Map<String, String> references, List<DemandeFileDTO> files, DemandeDTO demandeDTO) {
        LOGGER.info("Début archivage des documents pour les références {}", references);
        return archiverDocument(references, files, demandeDTO);
    }

    private List<String> archiverDocument(Map<String, String> references, List<DemandeFileDTO> files, DemandeDTO demandeDTO) {
        double progresArchivage = 0;
        double valeurStep = 1d / files.size();
        int demandeId = demandeDTO.getPkDemandes();
        AtomicBoolean erreurRio = new AtomicBoolean();
        boolean erreurConvertisseur = false;
        ArchivageStatutDTO archivageStatut = new ArchivageStatutDTO();
        archivageStatut.setAvancement(ArchivageStatutAvancementEnum.EN_COURS);
        archivageStatut.setProgression(progresArchivage);
        archivageProgress.put(demandeId, archivageStatut);
        ArchivageRapportExportDTO archivageRapportExportDTO = new ArchivageRapportExportDTO();

        Map<String, Integer> referencesTraitees = new HashMap<>();

        AtomicInteger fichiersEnErreurs = new AtomicInteger(0);

        // En début de process, on boucle sur les fichiers pour initialiser le rapport d'archivage
        // Si on le fait après, il pourrait y avoir une erreur qui stop le process
        for (DemandeFileDTO file : files) {
            archivageRapportExportDTO.addFichiersInitiaux(createFichierInitial(file));
        }

        List<RioDocumentDTO> rioDocumentDTOs = references.entrySet().stream()
                .map(entry -> getRioDocumentDTO(entry.getKey(), entry.getValue(), erreurRio))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (DemandeFileDTO file : files) {
            try {
                LOGGER.info("Génération des images TIFFs pour fichier {}", file.getName());
                Map<String, InputStream> filesTiff = convertisseurTiffService.generateTiffs(file);

                boolean erreurArchivageFichierCourant = false;
                LOGGER.info("Archivage des pages du document {}", file.getName());

                for (Map.Entry<String, InputStream> fileTiff : filesTiff.entrySet()) {
                    // Ajout dans la liste des fichiers qui ont bien été convertis
                    archivageRapportExportDTO.addFichiersConvertis(createFichierConverti(file, STATUT_OK, fileTiff.getKey()));

                    //On appel à l'archivage de la page courante
                    boolean erreurArchivagePageCourante = this.archiverPageFichier(erreurRio, archivageRapportExportDTO,
                            referencesTraitees, rioDocumentDTOs, file, fileTiff);

                    if (erreurArchivagePageCourante) {
                        archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_KO, fileTiff.getKey(), ""));
                        fichiersEnErreurs.incrementAndGet();
                        erreurArchivageFichierCourant = true;
                    }
                    // On fait avancer le step de l'archivage au prochain fichier
                    progresArchivage += valeurStep;
                    archivageStatut.setProgression(progresArchivage);
                }
                if (erreurArchivageFichierCourant) {
                    archivageStatut.setNbFichiersEnErreur(archivageStatut.getNbFichiersEnErreur() + 1);
                }
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la conversion du document {}", file.getName(), e);
                archivageRapportExportDTO.addFichiersConvertis(createFichierConverti(file, STATUT_KO, ""));
                archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_KO, "", ""));

                // On fait avancer les steps d'archivage
                progresArchivage += valeurStep;
                archivageStatut.setProgression(progresArchivage);

                erreurConvertisseur = true;
                archivageStatut.setNbFichiersEnErreur(archivageStatut.getNbFichiersEnErreur() + 1);
                fichiersEnErreurs.incrementAndGet();
            }
        }

        LOGGER.info("Fin archivage des documents");

        archivageRapportExportDTO.setDemarcheId(demandeDTO.getDemarcheId());
        archivageRapportExportDTO.setDemandeFlatDTO(afBackUtils.demandeDTOToDemandeFlatDTO(demandeDTO));

        String nomRapport = genererNomRapport(demandeDTO.getIdentifiant(), archivageRapportExportDTO);
        try (ByteArrayOutputStream rapport = generateArchivageRecap(archivageRapportExportDTO, demandeDTO, nomRapport)) {
            processErreursArchivage(erreurRio.get(), erreurConvertisseur, demandeDTO, rapport, nomRapport);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la génération du rapport d'archivage pour la demande {}", demandeId, e);
        }
        List<String> listeReferences = referencesTraitees.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (fichiersEnErreurs.get() > 0) {
            // Sauvegarde du numéro de facture dans les données de la demande
            demandesDataService.saveOrUpdateDemandeData(demandeDTO.getDemarcheId(), demandeId, NOMBRE_FICHIERS_ERREUR_ARCHIVAGE,
                    String.valueOf(fichiersEnErreurs.get()), false);
            histoService.actionSysteme(demandeId, "ECHEC", "Archivage automatique des fichiers en échec");
        } else {
            histoService.actionSysteme(demandeId, "SUCCES", "Archivage automatique des fichiers réalisé avec succès");
        }
        ArchivageStatutDTO statutDTO = new ArchivageStatutDTO();
        statutDTO.setAvancement(ArchivageStatutAvancementEnum.COMPLETE);
        statutDTO.setProgression(1d);
        archivageProgress.put(demandeId, statutDTO);
        demandesDataService.saveOrUpdateDemandeData(demandeDTO.getDemarcheId(), demandeId, ARCHIVAGE_RIO_COMPLETED, "true", false);

        return listeReferences;
    }

    private boolean archiverPageFichier(AtomicBoolean erreurRio, ArchivageRapportExportDTO archivageRapportExportDTO,
                                        Map<String, Integer> referencesTraites, List<RioDocumentDTO> rioDocumentDTOs,
                                        DemandeFileDTO file, Map.Entry<String, InputStream> fileTiff) {
        try (InputStream value = fileTiff.getValue()) {
            //Il faut sortir le tableau de bytes dans la boucle sinon, au deuxième élément, value.readAllBytes() renvoi un tableau vide
            byte[] bytes = value.readAllBytes();

            return this.envoyerPageDocumentEnGed(archivageRapportExportDTO, rioDocumentDTOs, file,
                    fileTiff, bytes, referencesTraites, erreurRio);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'archivage du document {}", file.getName(), e);
            return true;
        }
    }

    private boolean envoyerPageDocumentEnGed(ArchivageRapportExportDTO archivageRapportExportDTO, List<RioDocumentDTO> rioDocumentDTOs,
                                             DemandeFileDTO file, Map.Entry<String, InputStream> fileTiff, byte[] bytes,
                                             Map<String, Integer> referencesTraites, AtomicBoolean erreurRio) {
        boolean erreurMiseEnGed = false;
        String refDocument = StringUtils.EMPTY;
        for (RioDocumentDTO rioDocumentDTO : rioDocumentDTOs) {
            try {

                LOGGER.info("Envoi du documents en GED pour {}", fileTiff.getKey());

                refDocument = rioDocumentDTO.getRefDocument();
                rioService.createFileDocument(refDocument, fileTiff.getKey(), bytes, rioDocumentDTO.getCodeNotice());
                archivageRapportExportDTO.addFichiersDeposes(createFichierDepose(file, STATUT_OK, fileTiff.getKey(), refDocument));
                referencesTraites.putIfAbsent(refDocument, 0);
            } catch (Exception e) {
                LOGGER.error("Erreur lors de l'envoi du documents en GED pour {}", file.getName(), e);
                erreurMiseEnGed = true;
                referencesTraites.computeIfPresent(refDocument, (ref, nbErreurs) -> nbErreurs + 1);
                erreurRio.set(true);
            }
        }
        return erreurMiseEnGed;
    }

    private RioDocumentDTO getRioDocumentDTO(String ref, String codeType, AtomicBoolean erreurRio) {
        LOGGER.info("Vérification de l'existence du document");
        RioDocumentDTO rioDocumentDTO = null;
        String codeNotice = CODE_TYPE_IMMAT.equals(codeType) ? CODE_NOTICE_REGISTRE : CODE_NOTICE_PERMIS;
        try {
            rioDocumentDTO = rioService.getDocument(ref, codeNotice);
        } catch (HttpServerErrorException e) {
            // Si le document n'existe pas, nous devons le créer
            if (e.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                rioDocumentDTO = rioService.createDocument(ref, codeNotice);
            }
        } catch (Exception e) {
            LOGGER.info("Problème avec l'api RIO");
            erreurRio.set(true);
        }
        return rioDocumentDTO;
    }


    private void processErreursArchivage(boolean erreurRIO, boolean erreurConvertisseur, DemandeDTO demandeDTO, ByteArrayOutputStream rapport, String nomRapport) {
        if (erreurRIO) {
            sendMailProblemeRIO(demandeDTO, rapport, nomRapport);
        } else if (erreurConvertisseur) {
            sendMailProblemeConvertisseur(demandeDTO, rapport, nomRapport);
        }
    }

    private void sendMailProblemeConvertisseur(DemandeDTO demandeDTO, ByteArrayOutputStream rapport, String nomRapport) {
        String subjectTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CONVERTISSEUR_CORPS";
        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, InputStream> pj = new HashMap<>();
        pj.put(nomRapport, new ByteArrayInputStream(rapport.toByteArray()));
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), 8, null, pj);
    }

    private void sendMailProblemeRIO(DemandeDTO demandeDTO, ByteArrayOutputStream rapport, String nomRapport) {
        String subjectTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_OBJET";
        String bodyTemplateCode = "MAIL_RIO_ECHEC_ARCHIVAGE_CORPS";
        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_ADMIN_METIER.name());
        Map<String, InputStream> pj = new HashMap<>();
        pj.put(nomRapport, new ByteArrayInputStream(rapport.toByteArray()));
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, demandeDTO.getPkDemandes(), demandeDTO.getIdentifiant(), 9, null, pj);
    }

    private ByteArrayOutputStream generateArchivageRecap(ArchivageRapportExportDTO rapportExportDTO, DemandeDTO demandeDTO, String nomRapport) throws IOException {
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
        excelExportService.exportExcelSimple("rapport_archivage.xlsx", model, output);

        LOGGER.info("Sauvegarde du fichier...");
        ByteArrayOutputStream outputSave = new ByteArrayOutputStream();
        String url = fileService.saveFile(demandeDTO, nomRapport, gouvPropertiesResolver.getContainerId(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(output.toByteArray()), outputSave);

        outputSave.close();

        saveFichier(nomRapport, url, demandeDTO, demandeDTO.getDemarcheId());

        return output;
    }

    private String genererNomRapport(String identifiant, ArchivageRapportExportDTO rapportExportDTO) {
        StringBuilder builder = new StringBuilder();
        builder.append("Export_Archivage_");
        if (StringUtils.equals(rapportExportDTO.getCodeNotice(), CODE_NOTICE_PERMIS)) {
            builder.append("Permis_").append(rapportExportDTO.getRefDocument()).append('_');
        } else if (StringUtils.equals(rapportExportDTO.getCodeNotice(), CODE_NOTICE_REGISTRE)) {
            builder.append("Registre_").append(rapportExportDTO.getRefDocument()).append('_');
        }
        builder.append(identifiant).append('_').append(AfBackUtils.generateFileDateSuffix()).append(".xlsx");
        return builder.toString();
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

    private ArchivageFichierDeposeDTO createFichierDepose(DemandeFileDTO file, String statut, String nomTiff, String referenceDossier) {
        ArchivageFichierDeposeDTO fichierDeposeDTO = new ArchivageFichierDeposeDTO();
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);
        fichierDeposeDTO.setNom(file.getName());
        fichierDeposeDTO.setNomTiff(nomTiff);
        fichierDeposeDTO.setStatut(statut);
        fichierDeposeDTO.setDate(dateTimeString);
        fichierDeposeDTO.setReferenceDossier(referenceDossier);
        return fichierDeposeDTO;
    }
}
