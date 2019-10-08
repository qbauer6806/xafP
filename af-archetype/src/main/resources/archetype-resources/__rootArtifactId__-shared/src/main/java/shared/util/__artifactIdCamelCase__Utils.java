#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.af.back.util.DemandeHistoriqueComparator;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeHistoriqueDTO;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueContenuDTO;
import ${groupId}.shared.dto.${artifactIdCamelCase}DemandeHistoriqueDTO;
import ${groupId}.shared.model.v1568884433537.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classe utilitaire pour le projet ${artifactIdUpper}
 * 
 * @author mpavone
 *
 */
@Component
public class ${artifactIdCamelCase}Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}Utils.class);

    private static final JoursFeriesEnum[] joursFeries = JoursFeriesEnum.values();

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
     * @param demHistos
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

    private static boolean[] createJoursFeriesBooleanList(ContenuProjectDemandeDTO contenuDemande) {
        ProjectDemandeFieldDonneeDerogationJoursferiesDTO joursferies = contenuDemande.getDonnee().getDerogation().getJoursferies();
        return new boolean[] {false, joursferies.getSainteDevote(), joursferies.getLundiDePaques(), false, joursferies.getAscension(),
                joursferies.getLundiDePentecote(), joursferies.getFeteDieu(), joursferies.getAssomption(), joursferies.getToussaint(),
                false, joursferies.getImmaculeeConception(), false};
    }

    private static boolean[] createJoursFeriesExceptionnelsBooleanList(ContenuProjectDemandeDTO contenuDemande) {
        ProjectDemandeFieldDonneeDerogationJoursferiesDTO joursferies = contenuDemande.getDonnee().getDerogation().getJoursferies();
        return new boolean[] {joursferies.getJourDeL_An(), false, false, joursferies.getLe1erMai(), false, false, false, false, false,
                joursferies.getFeteDuPrince(), false, joursferies.getNoel()};
    }

    /**
     * Convertisseurs des jours feries exceptionnels selectionnés en phrase ou liste
     */
    public static String convertJourFeriesExceptionnelsToSentence(ContenuProjectDemandeDTO contenuDemande) {
        boolean[] joursFeriesExceptionnels = createJoursFeriesExceptionnelsBooleanList(contenuDemande);
        return convertSelectedJourFeriesTypesToString(joursFeriesExceptionnels);
    }

    public static String convertJourFeriesExceptionnelsToList(ContenuProjectDemandeDTO contenuDemande) {
        boolean[] joursFeriesExceptionnels = createJoursFeriesExceptionnelsBooleanList(contenuDemande);
        return convertSelectedJourFeriesTypesToStringList(joursFeriesExceptionnels);
    }

    /**
     * Convertisseurs des jours feries normaux selectionnés en phrase ou liste
     */
    public static String convertJourFeriesTypesToSentence(ContenuProjectDemandeDTO contenuDemande) {
        boolean[] jourFeriesBooleanArray = createJoursFeriesBooleanList(contenuDemande);
        return convertSelectedJourFeriesTypesToString(jourFeriesBooleanArray);
    }

    public static String convertJourFeriesTypesToList(ContenuProjectDemandeDTO contenuDemande) {
        boolean[] jourFeriesBooleanArray = createJoursFeriesBooleanList(contenuDemande);
        return convertSelectedJourFeriesTypesToStringList(jourFeriesBooleanArray);
    }

    public static String convertSelectedJourFeriesTypesToString(boolean ...jourFeriesSelected) {
        String formatedJFeries = "";
        for(int i = 0; i < jourFeriesSelected.length; i++) {
            if (jourFeriesSelected[i]) {
                formatedJFeries += ((StringUtils.isEmpty(formatedJFeries))? "" : ", ") + joursFeries[i].libelle;
            }
        }
        return formatedJFeries;
    }

    public static String convertSelectedJourFeriesTypesToStringList(boolean ...jourFeriesSelected) {
        StringBuilder formatedJFeries = new StringBuilder();
        for(int derogIndex = 0; derogIndex < jourFeriesSelected.length; derogIndex++) {
            if (jourFeriesSelected[derogIndex]) {
                formatedJFeries.append("${symbol_escape}n${symbol_escape}t- ").append(joursFeries[derogIndex].libelle);
            }
        }
        return formatedJFeries.append("${symbol_escape}n").toString();
    }
}
