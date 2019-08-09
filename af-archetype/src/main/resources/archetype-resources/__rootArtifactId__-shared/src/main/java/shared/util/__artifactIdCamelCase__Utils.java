#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.back.util.DemandeHistoriqueComparator;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeHistoriqueDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueContenuDTO;
import mc.gouv.${artifactIdLower}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.Emission2RouesEnum;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.EmissionVeloEnum;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.EmissionVoitureEnum;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.VehiculeTypetousEnum;

/**
 * Classe utilitaire pour le projet ${artifactIdUpper}
 * 
 * @author qdeme
 *
 */
@Component
public class ${artifactIdCamelCase}Utils {

    public static final String ${artifactIdUpper}_CALCULAIDE = "${artifactIdUpper}_CALCULAIDE";
    public static final String ${artifactIdUpper}_SUIVI_COMPTABLE = "${artifactIdUpper}_SUIVI_COMPTABLE";
    public static final String ${artifactIdUpper}_CALCULAIDE_FILE = "${artifactIdUpper}_CALCULAIDE_FILE";

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}Utils.class);

    /**
     * Permet de convertir une ligne d'historique DEM en une ligne d'historique ${artifactIdUpper} avec tous les détails
     * spécifiques à ${artifactIdUpper}.
     * 
     * @param demHisto
     * @return
     */
    public static ${artifactIdCamelCase}DemandeHistoriqueDTO histoDem2${artifactIdCamelCase}(DemandeHistoriqueDTO demHisto) {
        ${artifactIdCamelCase}DemandeHistoriqueDTO ${artifactIdLower}Histo = new ${artifactIdCamelCase}DemandeHistoriqueDTO();
        ${artifactIdLower}Histo.setDemHistorique(demHisto);
        ${artifactIdCamelCase}DemandeHistoriqueContenuDTO contenu = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contenu = mapper.treeToValue(demHisto.getContenu(), ${artifactIdCamelCase}DemandeHistoriqueContenuDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur", e);
        }
        ${artifactIdLower}Histo.setContenu(contenu);
        return ${artifactIdLower}Histo;
    }

    /**
     * Permet de convertir un ensemble de lignes d'historique DEM en un ensemble de lignes d'historique ${artifactIdUpper} avec
     * tous les détails spécifiques à ${artifactIdUpper}.
     * 
     * @param demHisto
     * @return
     */
    public static List<${artifactIdCamelCase}DemandeHistoriqueDTO> histoDem2${artifactIdCamelCase}(List<DemandeHistoriqueDTO> demHistos) {

        // Trier l'historique, au cas où (${symbol_pound}9597)
        Collections.sort(demHistos, new DemandeHistoriqueComparator());

        List<${artifactIdCamelCase}DemandeHistoriqueDTO> ${artifactIdLower}Histos = new ArrayList<${artifactIdCamelCase}DemandeHistoriqueDTO>();
        for (DemandeHistoriqueDTO demHisto : demHistos) {
            ${artifactIdLower}Histos.add(histoDem2${artifactIdCamelCase}(demHisto));
        }
        return ${artifactIdLower}Histos;
    }

    /**
     * Permet de créer une ligne d'historique pour DEM à partir des données d'historique spécifiques à ${artifactIdUpper}
     * 
     * @param ${artifactIdLower}HistoContenu
     * @param usagerId
     * @param agentId
     * @return
     */
    public static DemandeHistoriqueDTO histo${artifactIdCamelCase}2Dem(${artifactIdCamelCase}DemandeHistoriqueContenuDTO ${artifactIdLower}HistoContenu,
            Integer usagerId,
            String agentId) {
        DemandeHistoriqueDTO demHisto = new DemandeHistoriqueDTO();
        demHisto.setAgentId(agentId);
        demHisto.setUsagerId(usagerId);
        ObjectMapper mapper = new ObjectMapper();
        demHisto.setContenu(mapper.valueToTree(${artifactIdLower}HistoContenu));
        return demHisto;
    }

    public static ContenuProjectDemandeDTO getContenuDemande(DemandeDTO demande) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.treeToValue(demande.getContenu(), ContenuProjectDemandeDTO.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur", e);
        }
        return null;
    }

    public static BigDecimal convertStringToBigDecimal(String decimalStr) {
        // BigDecimal decimalValue = new BigDecimal(0.00);
        final String regexDecimal = "[0-9]*${symbol_escape}${symbol_escape},?[0-9]*";
        final String regexInteger = "[0-9]*";

        if (StringUtils.isBlank(decimalStr)) {
            return null;
        }

        if (decimalStr.matches(regexInteger) || decimalStr.matches(regexDecimal)) {
            return new BigDecimal(decimalStr.replace(",", "."));
        }

        return null;
        // return decimalValue;
    }

    public static String convertBigDecimalToString(BigDecimal decimalValue) {

        if (decimalValue != null) {
            return decimalValue.toString().replace(".", ",");
        }

        // return "0";
        return "";
    }

    public static String getVehiculeEmission(ContenuProjectDemandeDTO contenuDemande) {
        String emission = "";
        VehiculeTypetousEnum typeVehicule = contenuDemande.getDonnee().getVehiculetypetous();

        if (typeVehicule.equals(VehiculeTypetousEnum.CAT1)) {
            return EmissionVoitureEnum
                    .forValue(contenuDemande.getDonnee().getVehicule().getEmissionvoiture().name()).originalName;
        }
        if (typeVehicule.equals(VehiculeTypetousEnum.CAT2)) {
            return Emission2RouesEnum
                    .forValue(contenuDemande.getDonnee().getVehicule().getEmissiondeuxroues().name()).originalName;
        }
        if (typeVehicule.equals(VehiculeTypetousEnum.CAT3)) {
            return EmissionVeloEnum
                    .forValue(contenuDemande.getDonnee().getVehicule().getEmissionvelo().name()).originalName;
        }
        return emission;
    }

    public static String getVehiculeEmissionLibelle(ContenuProjectDemandeDTO contenuDemande) {
        String emission = "";
        VehiculeTypetousEnum typeVehicule = contenuDemande.getDonnee().getVehiculetypetous();

        if (typeVehicule.equals(VehiculeTypetousEnum.CAT1)) {
            return EmissionVoitureEnum
                    .getLibelle(contenuDemande.getDonnee().getVehicule().getEmissionvoiture().name());
        }
        if (typeVehicule.equals(VehiculeTypetousEnum.CAT2)) {
            return Emission2RouesEnum
                    .getLibelle(contenuDemande.getDonnee().getVehicule().getEmissiondeuxroues().name());
        }
        if (typeVehicule.equals(VehiculeTypetousEnum.CAT3)) {
            return EmissionVeloEnum
                    .getLibelle(contenuDemande.getDonnee().getVehicule().getEmissionvelo().name());
        }
        return emission;
    }

    public static String convertDateToSring(final Date date) {
        if (date == null) {
            return " ";
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public static String getSafeString(final String value) {
        return StringUtils.isBlank(value) ? " " : value;
    }

    public static String converDateString(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

}
