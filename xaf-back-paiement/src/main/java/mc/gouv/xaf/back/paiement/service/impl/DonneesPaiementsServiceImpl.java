package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementDemandeDataKeysEnum;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;
import mc.gouv.xaf.back.paiement.service.DonneesPaiementsService;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DonneesPaiementsServiceImpl implements DonneesPaiementsService {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

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
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        Integer demandeId = demande.getPkDemandes();

        DemandeDataDTO statutPaiement = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.STATUT_PAIEMENT.name());
        if (statutPaiement != null && StringUtils.isNotBlank(statutPaiement.getValue())) {
            PaiementStatutEnum statutEnum = PaiementStatutEnum.valueOf(statutPaiement.getValue());
            mav.addObject("statutPaiementCode", statutEnum.name());
            mav.addObject("statutPaiement", statutEnum.getLibelle());
            StatutPublicOuInterneDTO statutPaiementDTO = new StatutPublicOuInterneDTO();
            statutPaiementDTO.setName(statutEnum.name());
            mav.addObject("statutPaiementColor", demarchesDataProvider.getStatusColorClass(statutPaiementDTO));
        }
        DemandeDataDTO dateExpirationEmpreinte = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.DATE_EXPIRATION_EMPREINTE.name());
        if (dateExpirationEmpreinte != null && StringUtils.isNotBlank(dateExpirationEmpreinte.getValue())) {
            mav.addObject("dateExpirationEmpreinte",
                    LocalDate.parse(dateExpirationEmpreinte.getValue(), DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                            .format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)));
        }

        CommandeDemandeDTO commandeDemandeDTO = commandesDemandesService.getDerniereCommandeDemande(demandeId);
        if (null != commandeDemandeDTO) {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            mav.addObject("montantPaiement", format.format(commandeDemandeDTO.getMontant()));
        }

        DemandeDataDTO datePaiement = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.DATE_PAIEMENT.name());
        if (datePaiement != null && StringUtils.isNotBlank(datePaiement.getValue())) {
            mav.addObject("datePaiement",
                    LocalDate.parse(datePaiement.getValue(), DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                            .format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT)));
        }
        DemandeDataDTO numeroFacture = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.NUMERO_FACTURE.name());
        if (numeroFacture != null && StringUtils.isNotBlank(numeroFacture.getValue())) {
            mav.addObject("numeroFacture", numeroFacture.getValue());
        }
        mav.addObject("paiementHisto", paiementHistoriqueService.findAllByDemandeId(demandeId));
        mav.addObject("formatDate", DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));

        DemandeDataDTO moyenPaiement = demandesDataService.getDemandeData(demarcheId, demandeId, PaiementDemandeDataKeysEnum.MOYEN_PAIEMENT.name());
        if (moyenPaiement != null && StringUtils.isNotBlank(moyenPaiement.getValue())) {
            mav.addObject("moyenPaiement", moyenPaiement.getValue());
        }
    }

}
