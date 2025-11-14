package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FactureServiceImpl implements FactureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactureServiceImpl.class);

    public static final String PREFIX_FACTURE = "Justificatif_Facture_";
    public static final String PREFIX_JUSTIFICATIF_REÇU_PAIEMENT = "Justificatif_Reçu_Paiement_";

    @Autowired
    private FileService fileService;
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    @Autowired
    private DemandesFilesService demandesFilesService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private FactureApiClient factureApiClient;

    @Autowired
    private DemandesDataService demandesDataService;

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
        try {
            String fileName = PREFIX_JUSTIFICATIF_REÇU_PAIEMENT + identifiantDemande + "_" + AfBackUtils.generateFileDateSuffix() + ".pdf";
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempFile = new File(tempDir, fileName);

            // Créer le fichier (s'il n'existe pas encore)
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            file.transferTo(tempFile);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String url = fileService.saveFile(demande, tempFile.getName(), gouvPropertiesResolver.getContainerId(),
                    "application/pdf", new FileInputStream(tempFile), output);
            saveFichier(tempFile.getName(), url, demande);
            tempFile.deleteOnExit();
            output.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
