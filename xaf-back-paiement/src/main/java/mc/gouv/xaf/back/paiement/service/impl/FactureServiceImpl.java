package mc.gouv.xaf.back.paiement.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.service.FactureService;
import mc.gouv.xaf.back.paiement.service.ReferenceFactoryService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

@Service
public class FactureServiceImpl implements FactureService {
    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    public static final String PREFIX_FACTURE = "Justificatif_Facture_";

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
    public void saveFacture(String reference, Integer demandeId) throws Exception {
        logStartMethod(LOGGER);
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        DemandeDTO demande = demandesService.getDemande(demarcheId, demandeId);
        Optional<InputStream> optionalFactureIS = factureApiClient.getFacture(reference, demande);
        if (optionalFactureIS.isPresent()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String fileName = PREFIX_FACTURE + demande.getIdentifiant() + "_" + AfBackUtils.generateFileDateSuffix() + ".pdf";
            String url = fileService.saveFile(demande, fileName, gouvPropertiesResolver.getContainerId(), "application/pdf", optionalFactureIS.get(), output);
            output.close();
            optionalFactureIS.get().close();
            saveFichier(fileName, url, demande);

            // Sauvegarde du numéro de facture dans les données de la demande
            demandesDataService.saveOrUpdateDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name(), reference);
        }
    }

    private void saveFichier(String fileName, String url, DemandeDTO demande) {
        logStartMethod(LOGGER);
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(fileName);
        file.setUrl('/' + url);
        file.setDate(new Date());
        file.setMeta("BACK_FRONT_JUSTIFICATIF_DEMANDE");
        demandesFilesService.saveFile(file, gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
    }
}
