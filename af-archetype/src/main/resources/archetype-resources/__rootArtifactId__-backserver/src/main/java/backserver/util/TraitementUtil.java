#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.backserver.util;

import mc.gouv.${artifactIdLower}.backserver.formbean.CalculAideFormBean;
import mc.gouv.${artifactIdLower}.backserver.formbean.SuiviComptableFormBean;
import mc.gouv.${artifactIdLower}.shared.dto.CalculAideDTO;
import mc.gouv.${artifactIdLower}.shared.dto.SuiviComptableDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.VehiculeTypetousEnum;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;

public class TraitementUtil {

    public static CalculAideDTO mapCalculAideFormToDTO(CalculAideFormBean form) {

        CalculAideDTO dto = new CalculAideDTO();
        dto.setTypeUsager(form.getTypeUsager());
        dto.setApplicationPourcentage(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getApplicationPourcentage()));
        dto.setCommentCGD(form.getCommentCGD());
        dto.setEmission(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getEmission()));
        dto.setMontantAide(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getMontantAide()));
        dto.setMontantBatterie(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getMontantBatterie()));
        dto.setMontantSimule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getMontantSimule()));
        dto.setMontantSimulePlus20(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getMontantSimulePlus20()));
        dto.setMontantSimuleMoins20(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getMontantSimuleMoins20()));
        dto.setPrimeForfaitaire(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrimeForfaitaire()));
        dto.setPrimeTaxi(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrimeTaxi()));
        dto.setPrimeCalcule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrimeCalcule()));
        dto.setPrixBasVehicule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrixBasVehicule()));
        dto.setPrixTotalVehicule(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrixTotalVehicule()));
        dto.setPuissance(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPuissance()));
        dto.setPrixTotal(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getPrixTotal()));
        dto.setRemiseDeduire(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getRemiseDeduire()));
        dto.setTva(${artifactIdCamelCase}Utils.convertStringToBigDecimal(form.getTva()));

        return dto;
    }

    public static CalculAideFormBean mapCalculAideDTO2FormBean(CalculAideDTO dto) {

        CalculAideFormBean form = new CalculAideFormBean();

        if (dto != null) {
            form.setApplicationPourcentage(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getApplicationPourcentage()));
            form.setCommentCGD(dto.getCommentCGD());
            form.setEmission(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getEmission()));
            form.setMontantAide(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantAide()));
            form.setMontantBatterie(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantBatterie()));
            form.setMontantSimule(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantSimule()));
            form.setMontantSimulePlus20(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantSimulePlus20()));
            form.setMontantSimuleMoins20(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantSimuleMoins20()));
            form.setPrimeForfaitaire(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrimeForfaitaire()));
            form.setPrimeTaxi(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrimeTaxi()));
            form.setPrimeCalcule(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrimeCalcule()));
            form.setPrixBasVehicule(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrixBasVehicule()));
            form.setPrixTotalVehicule(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrixTotalVehicule()));
            form.setPuissance(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPuissance()));
            form.setPrixTotal(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrixTotal()));
            form.setRemiseDeduire(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getRemiseDeduire()));
            form.setTva(${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getTva()));
            form.setTypeUsager(dto.getTypeUsager());
        }

        return form;
    }

    public static SuiviComptableDTO mapSuiviComptableFormToDTO(SuiviComptableFormBean form) {
        SuiviComptableDTO dto = new SuiviComptableDTO();

        dto.setArticle(form.getArticle());
        dto.setNumeroOrdre(form.getNumeroOrdre());
        dto.setFed(form.getFed());
        dto.setExercice(form.getExercice());

        return dto;
    }

    public static SuiviComptableFormBean mapSuiviComptableDTO2Form(SuiviComptableDTO dto) {

        SuiviComptableFormBean bean = new SuiviComptableFormBean();
        bean.setArticle(dto.getArticle());
        bean.setExercice(dto.getExercice());
        bean.setFed(dto.getFed());
        bean.setNumeroOrdre(dto.getNumeroOrdre());

        return bean;
    }

    public static String getVehiculeEmissionCode(ContenuProjectDemandeDTO contenuDemande) {
        VehiculeTypetousEnum typeVehicule = contenuDemande.getDonnee().getVehiculetypetous();

        if (typeVehicule == VehiculeTypetousEnum.CAT1) {
            return contenuDemande.getDonnee().getVehicule().getEmissionvoiture().originalName;
        }
        if (typeVehicule == VehiculeTypetousEnum.CAT2) {
            return contenuDemande.getDonnee().getVehicule().getEmissiondeuxroues().originalName;
        }
        if (typeVehicule == VehiculeTypetousEnum.CAT3) {
            return contenuDemande.getDonnee().getVehicule().getEmissionvelo().originalName;
        }

        return "";
    }

    /*
     * public static DemandeFileDTO[] getDemandePieceJustificatif(DemandeDTO demande) {
     * 
     * return (DemandeFileDTO[]) Arrays.stream(demande.getFichiers()) .filter(demandeFile ->
     * StringUtils.isBlank(demandeFile.getMeta())) .toArray();
     * 
     * return null; }
     * 
     * public static DemandeFileDTO[] getDemandeFichiersInterne(DemandeDTO demande) {
     * 
     * return (DemandeFileDTO[]) (Arrays.stream(demande.getFichiers()) .filter(demandeFile ->
     * StringUtils.isNoneBlank(demandeFile.getMeta())) .toArray());
     * 
     * return null; }
     */
}
