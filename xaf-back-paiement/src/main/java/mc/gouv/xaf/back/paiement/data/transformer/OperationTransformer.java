package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author mpavone.ext
 */
public class OperationTransformer {

    private OperationTransformer() {
    }

    public static OperationDTO bo2Dto(OperationBO bo) {
        OperationDTO dto = new OperationDTO();
        dto.setPkOperation(bo.getPkOperation());
        dto.setOperationType(OperationTypeTransformer.bo2String(bo.getOperationType()));
        dto.setOperationStatut(OperationStatutTransformer.bo2String(bo.getOperationStatut()));
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerniereModification(bo.getDateDerniereModification());
        dto.setMontant(bo.getMontant());
        dto.setNumeroAutorisation(bo.getNumeroAutorisation());
        dto.setNumeroFacture(bo.getNumeroFacture());
        dto.setCodeRetour(bo.getCodeRetour());
        dto.setLibelle(bo.getLibelle());
        return dto;
    }

    public static OperationBO dto2Bo(OperationDTO dto) {
        OperationBO bo = new OperationBO();
        bo.setPkOperation(dto.getPkOperation());
        bo.setOperationType(OperationTypeTransformer.string2Bo(dto.getOperationType()));
        bo.setOperationStatut(OperationStatutTransformer.string2Bo(dto.getOperationStatut()));
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerniereModification(dto.getDateDerniereModification());
        bo.setMontant(dto.getMontant());
        bo.setNumeroAutorisation(dto.getNumeroAutorisation());
        bo.setNumeroFacture(dto.getNumeroFacture());
        bo.setCodeRetour(dto.getCodeRetour());
        bo.setLibelle(dto.getLibelle());
        return bo;
    }

    public static List<OperationDTO> bos2Dtos(List<OperationBO> bos) {
        ArrayList<OperationDTO> dtos = new ArrayList<>();
        for (OperationBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<OperationBO> dtos2Bos(List<OperationDTO> dtos) {
        ArrayList<OperationBO> bos = new ArrayList<>();
        for (OperationDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

    public static String toCSV(OperationDTO operationDTO) {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add(operationDTO.getPkOperation());
        csvString.add(operationDTO.getOperationType()== null ? "null" :operationDTO.getOperationType());
        csvString.add(operationDTO.getOperationStatut()== null ? "null" :operationDTO.getOperationStatut());
        csvString.add(operationDTO.getDateCreation().toString());
        csvString.add(operationDTO.getDateDerniereModification().toString());
        csvString.add("" + operationDTO.getMontant());
        csvString.add("" + operationDTO.getNumeroAutorisation());
        csvString.add(operationDTO.getNumeroFacture());
        return csvString.toString();
    }

    public static String headerCSV() {
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
