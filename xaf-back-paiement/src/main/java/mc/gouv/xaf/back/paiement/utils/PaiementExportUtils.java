package mc.gouv.xaf.back.paiement.utils;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;

import java.util.StringJoiner;

public class PaiementExportUtils {

    public static String toCSV(CommandeDTO commandeDTO) {
        StringJoiner csvString = new StringJoiner(";");
        MoyenPaiementDTO moyenPaiementDTO = commandeDTO.getMoyenPaiement();
        csvString.add(moyenPaiementDTO.getPkMoyenPaiements());
        csvString.add(moyenPaiementDTO.getCodeSociete());
        csvString.add(moyenPaiementDTO.getDateLimite().toString());
        csvString.add("" + commandeDTO.getMontantInitial());
        csvString.add("" + commandeDTO.getMontantDejaCapture());
        csvString.add("" + commandeDTO.getMontantRestant());
        csvString.add(moyenPaiementDTO.getMoyenPaiementType() == null ? "null" :moyenPaiementDTO.getMoyenPaiementType());
        csvString.add(moyenPaiementDTO.getMoyenPaiementStatut()== null ? "null" :moyenPaiementDTO.getMoyenPaiementStatut());
        csvString.add(moyenPaiementDTO.getDateDerniereModification().toString());
        csvString.add(moyenPaiementDTO.getCvx());
        csvString.add(moyenPaiementDTO.getVld());
        csvString.add(moyenPaiementDTO.getBrand());
        csvString.add(moyenPaiementDTO.getNumauto());
        csvString.add(moyenPaiementDTO.getUsage());
        csvString.add(moyenPaiementDTO.getTypecompte());
        csvString.add(moyenPaiementDTO.getEcard());
        csvString.add(moyenPaiementDTO.getCbmasquee());
        csvString.add(moyenPaiementDTO.getOriginecb());
        csvString.add(moyenPaiementDTO.getBincb());
        csvString.add(moyenPaiementDTO.getHpancb());
        csvString.add(moyenPaiementDTO.getIpclient());
        csvString.add(moyenPaiementDTO.getOriginetr());
        csvString.add(moyenPaiementDTO.getModepaiement());
        csvString.add(moyenPaiementDTO.getAuthentification());
        csvString.add(moyenPaiementDTO.getLangue());
        csvString.add(moyenPaiementDTO.getMac());
        return csvString.toString();
    }

    public static String headerCommandeCSV() {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add("pkMoyenPaiement");
        csvString.add("codeSociete");
        csvString.add("dateLimite");
        csvString.add("montantInitial");
        csvString.add("montantCapture");
        csvString.add("montantRestant");
        csvString.add("moyenPaiementType");
        csvString.add("moyenPaiementStatut");
        csvString.add("dateDerniereModification");
        csvString.add("cvx");
        csvString.add("vld");
        csvString.add("brand");
        csvString.add("numauto");
        csvString.add("usage");
        csvString.add("typecompte");
        csvString.add("ecard");
        csvString.add("cbmasquee");
        csvString.add("originecb");
        csvString.add("bincb");
        csvString.add("hpancb");
        csvString.add("ipclient");
        csvString.add("originetr");
        csvString.add("modepaiement");
        csvString.add("authentification");
        csvString.add("langue");
        csvString.add("mac");
        return csvString.toString();
    }

    public static String toCSV(CommandeOperationDTO commandeOperationDTO) {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add(commandeOperationDTO.getPkOperations());
        csvString.add(commandeOperationDTO.getOperationType()== null ? "null" : commandeOperationDTO.getOperationType());
        csvString.add(commandeOperationDTO.getOperationStatut()== null ? "null" : commandeOperationDTO.getOperationStatut());
        csvString.add(commandeOperationDTO.getDateCreation().toString());
        csvString.add(commandeOperationDTO.getDateDerniereModification().toString());
        csvString.add("" + commandeOperationDTO.getMontant());
        csvString.add("" + commandeOperationDTO.getNumeroAutorisation());
        csvString.add(commandeOperationDTO.getNumeroFacture());
        return csvString.toString();
    }

    public static String headerOperationCSV() {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add("pkOperation");
        csvString.add("operationType");
        csvString.add("operationStatut");
        csvString.add("dateCreation");
        csvString.add("dateDerniereModification");
        csvString.add("montant");
        csvString.add("numeroAuthorisation");
        csvString.add("numeroFacture");
        return csvString.toString();
    }
}
