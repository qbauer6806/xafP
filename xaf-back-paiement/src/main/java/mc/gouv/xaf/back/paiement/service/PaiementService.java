package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import mc.gouv.xaf.back.paiement.dto.DebitDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.paiement.MwpaymtGenericCallbackDTO;
import mc.gouv.xaf.shared.paiement.infofacturation.InfoFacturationResponseDTO;
import mc.gouv.xaf.shared.paiement.mongichet.PaymentMethodReferenceDTO;
import mc.gouv.xaf.shared.paiement.moyenpaiement.MoyenPaiementInputDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import java.util.List;

public interface PaiementService {

    List<TableauDTO> getTableauPaiement(String ids, String objectType, Integer usagerId);
    InfoFacturationResponseDTO getInfoFacturation(GichuniUsagerDTO usager);
    boolean createMoyenPaiement(String demandeIds, GichuniUsagerDTO usager, String orderId, String raisonSociale, String langue);
    void updateMoyenPaiement(MoyenPaiementInputDTO moyenPaiementInputDTO, String usagerToken);
    PaymentMethodInformationDTO getMoyenPaiement(InfoCancelInputDTO input, String usagerToken);
    List<PaymentMethodReferenceDTO> getReferences(String usagerToken);
    void updatePaiementStatus(MwpaymtGenericCallbackDTO callbackDTO);
    void updatePaiementStatusAsync(MwpaymtGenericCallbackDTO callbackDTO);
    RegisterOutputDTO postInfoPaiement(RegisterInputDTO input, String usagerToken);
    void majTarif(Integer tarif);
    void majStatutCaisse(String authorization);
    DebitDTO debit(String idTs, String orderIdResid, String authorization);
    void envoiMailAgent(String idTs);
    boolean isDebitDeclenche(Integer pkDemande);
}
