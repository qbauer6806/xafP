package mc.gouv.xaf.back.paiement.service.impl;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.service.DonneesPaiementsService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
public class DonneesPaiementsServiceImpl implements DonneesPaiementsService {

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private CommandesDemandesService commandesDemandesService;

    @Autowired
    private PaiementHistoriqueService paiementHistoriqueService;

    @Override
    public void chargerDonneesPaiement(ModelAndView mav, DemandeDTO demande) {
        Integer demandeId = demande.getPkDemandes();

        DemandeDataDTO statutPaiement = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());
        if (statutPaiement != null && StringUtils.isNotBlank(statutPaiement.getValue())) {
            PaiementStatutEnum statutEnum = PaiementStatutEnum.valueOf(statutPaiement.getValue());
            mav.addObject("statutPaiementCode", statutEnum.name());
            mav.addObject("statutPaiement", statutEnum.getLibelle());
            mav.addObject("statutPaiementColor", demarchesDataProvider.getStatusColorClass(statutEnum.name()));
        }
        DemandeDataDTO dateExpirationEmpreinte = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.DATE_EXPIRATION_EMPREINTE.name());
        if (dateExpirationEmpreinte != null && StringUtils.isNotBlank(dateExpirationEmpreinte.getValue())) {
            mav.addObject("dateExpirationEmpreinte",
                    LocalDate.parse(dateExpirationEmpreinte.getValue(), DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                            .format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)));
        }

        mav.addObject("montantPaiement", getMontant(demande));

        DemandeDataDTO datePaiement = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name());
        if (datePaiement != null && StringUtils.isNotBlank(datePaiement.getValue())) {
            mav.addObject("datePaiement",
                    LocalDate.parse(datePaiement.getValue(), DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                            .format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)));
        }
        DemandeDataDTO numeroFacture = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name());
        if (numeroFacture != null && StringUtils.isNotBlank(numeroFacture.getValue())) {
            mav.addObject("numeroFacture", numeroFacture.getValue());
        }
        mav.addObject("paiementHisto", paiementHistoriqueService.findAllByDemandeId(demandeId));
        mav.addObject("formatDate", DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));

        DemandeDataDTO moyenPaiement = demandesDataService.getDemandeData(demandeId,
                PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT.name());
        if (moyenPaiement != null && StringUtils.isNotBlank(moyenPaiement.getValue())) {
            mav.addObject("moyenPaiement", moyenPaiement.getValue());
        }
    }

    private String getMontant(DemandeDTO demandeDTO) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        if (demarchesDataProvider.getDemarcheCanHandleTaches()) {
            DemandeDataDTO dataMontant = demandesDataService.getDemandeData(demandeDTO.getPkDemandes(),
                    PaiementDemandeDataKeysEnum.MONTANT_PAYE.name());
            if (null != dataMontant) {
                return format.format(Double.parseDouble(dataMontant.getValue()));
            }
        }
        CommandeDemandeDTO commandeDemandeDTO = commandesDemandesService.getDerniereCommandeDemande(
                demandeDTO.getPkDemandes());
        if (null != commandeDemandeDTO) {
            return format.format(commandeDemandeDTO.getMontant());
        }
        return "";
    }
}
