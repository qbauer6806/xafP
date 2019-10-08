#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.back.xls.ExcelExportModelProvider;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import ${groupId}.shared.dto.DemandeExcelFlatDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ExcelExportModelProviderImpl implements ExcelExportModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExportModelProviderImpl.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private ${artifactIdCamelCase}Utils ${artifactIdLower}Utils;

    @Override
    public Map<String, Object> getModel(String plainStartDate, String plainEndDate) {

        LOGGER.info("ExcelExportModelProviderImpl.getModel()");

        Map<String, Object> model = new HashMap<>();
        List<Object> demandesFlat = new ArrayList<>();

        List<DemandeDTO> listAll = retrieveDemandesFilteredByDate(plainStartDate, plainEndDate);
        LOGGER.info("Récupération de la liste des demandes filtrées par date...");

        listAll.forEach(demande -> {
            DemandeExcelFlatDTO demandeFlatDTO = new DemandeExcelFlatDTO(afBackUtils.getDemandeFlatDTO(demande),
                    ${artifactIdCamelCase}Utils.getContenuDemande(demande));

            demandeFlatDTO.setEtatInterne(getEtatIntern(demande));
            demandesFlat.add(demandeFlatDTO);
        });

        model.put("demandes", demandesFlat);
        model.put("${artifactIdLower}Utils", ${artifactIdLower}Utils);
        model.put("afBackUtils", afBackUtils);

        LOGGER.info("Fin ExcelExportModelProviderImpl.getModel()");

        return model;
    }

    List<DemandeDTO> retrieveDemandesFilteredByDate(String plainStartDate, String plainEndDate) {
        List<DemandeDTO> demandeDTOS;
        try {
            Date startDate = null;
            Date endDate = null;
            if (StringUtils.isNotEmpty(plainStartDate)) {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(plainStartDate);
            }
            if (StringUtils.isNotEmpty(plainEndDate)) {
                endDate = new SimpleDateFormat("dd/MM/yyyy").parse(plainEndDate);

                // Last moment of days
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
                endDate = cal.getTime();
            }

            demandeDTOS = demandesService.getAllDemandesFilteredByDate(gouvPropertiesResolver.getDemarcheId(), startDate, endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            LOGGER.error("Problème dans le parsing des dates, recherche sur toutes les demandes");
            demandeDTOS = demandesService.getAllDemandes(gouvPropertiesResolver.getDemarcheId());
        }

        return demandeDTOS;
    }

    private String getEtatIntern(DemandeDTO demande) {
        StatutPublicOuInterneDTO statut = afBackUtils.getStatutPublicOuInterne(demande);
        return statut.getLibelle();
    }

}
