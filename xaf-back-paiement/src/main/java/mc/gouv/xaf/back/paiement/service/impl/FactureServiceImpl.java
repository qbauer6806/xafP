package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactureServiceImpl.class);

    public static final String PREFIX_FACTURE = "Justificatif_Facture_";
    public static final String PREFIX_JUSTIFICATIF_RECU_PAIEMENT = "Justificatif_Reçu_Paiement_";

    private final FileService fileService;

    private final GouvPropertiesResolver gouvPropertiesResolver;

    private final DemandesFilesService demandesFilesService;

    private final DemandesService demandesService;

    private final FactureApiClient factureApiClient;

    private final DemandesDataService demandesDataService;

    @Override
    public void saveFacture(String reference, Integer demandeId) throws IOException {
        logStartMethod(LOGGER);

        if (StringUtils.isEmpty(reference) || StringUtils.equals(FactureApiClient.INCIDENT, reference)) {
            throw new DemarchesServiceException("Le numéro de la facture est incorrect", HttpStatus.BAD_REQUEST);
        }

        DemandeDTO demande = demandesService.getDemande(demandeId);
        Optional<InputStream> optionalFactureIS = factureApiClient.getFacture(reference, demande);
        if (optionalFactureIS.isPresent()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String fileName =
                    PREFIX_FACTURE + demande.getIdentifiant() + "_" + AfBackUtils.generateFileDateSuffix() + ".pdf";
            String url = fileService.saveFile(demande, fileName, gouvPropertiesResolver.getContainerId(),
                    "application/pdf", optionalFactureIS.get(), output);
            output.close();
            optionalFactureIS.get().close();
            saveFichier(fileName, url, demande);

            // Sauvegarde du numéro de facture dans les données de la demande
            demandesDataService.saveOrUpdateDemandeData(demandeId, PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name(),
                    reference);
        }
    }

    @Override
    public void saveRecuPaiement(String identifiantDemande, MultipartFile file) {
        DemandeDTO demande = demandesService.getDemande(identifiantDemande);

        // Sanitize l'identifiant pour éviter les path traversal
        String sanitizedIdentifiant = FileUtils.sanitizeFileName(identifiantDemande);

        String fileName =
                PREFIX_JUSTIFICATIF_RECU_PAIEMENT + sanitizedIdentifiant + "_" + AfBackUtils.generateFileDateSuffix()
                        + ".pdf";

        Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir")).normalize();
        Path tempFilePath = tempDirPath.resolve(fileName).normalize();

        // Vérification de sécurité : s'assurer que le fichier reste dans le répertoire temporaire
        if (!tempFilePath.startsWith(tempDirPath)) {
            throw new DemarcheException("Nom de fichier invalide détecté.");
        }

        File tempFile = tempFilePath.toFile();

        try {
            file.transferTo(tempFile);

            try (FileInputStream inputStream = new FileInputStream(tempFile);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                String url = fileService.saveFile(
                        demande,
                        tempFile.getName(),
                        gouvPropertiesResolver.getContainerId(),
                        "application/pdf",
                        inputStream,
                        outputStream
                );

                saveFichier(tempFile.getName(), url, demande);
            }

        } catch (IOException ex) {
            throw new DemarcheException("Erreur lors de l'enregistrement du reçu de paiement.", ex);
        } finally {
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException e) {
                LOGGER.warn("Échec de suppression du fichier temporaire pour la demande", e);
            }
        }
    }

    private void saveFichier(String fileName, String url, DemandeDTO demande) {
        logStartMethod(LOGGER);
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(fileName);
        file.setUrl('/' + url);
        file.setDate(new Date());
        file.setMeta("BACK_FRONT_JUSTIFICATIF_DEMANDE");
        demandesFilesService.saveFile(file, demande.getPkDemandes());
    }
}
