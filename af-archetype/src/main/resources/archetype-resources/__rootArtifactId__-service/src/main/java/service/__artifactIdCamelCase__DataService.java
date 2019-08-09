#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesDataService;
import mc.gouv.dem.shared.model.DemandeDataDTO;
import mc.gouv.${artifactIdLower}.shared.dto.CalculAideDTO;
import mc.gouv.${artifactIdLower}.shared.dto.SuiviComptableDTO;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;

@Component
public class ${artifactIdCamelCase}DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}DataService.class);

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public CalculAideDTO getCalculAideDTO(final Integer demandeID) {
        CalculAideDTO calculAideDTO = new CalculAideDTO();

        Field[] calculAideDtoFields = CalculAideDTO.class.getDeclaredFields();
        List<DemandeDataDTO> demandesData = demandesDataService.getDemandeDatas(gouvPropertiesResolver.getDemarcheId(),
                demandeID);

        demandesData.stream().filter(d -> !StringUtils.isBlank(d.getValue())).forEach(data -> {
            try {

                Optional<Field> field = Arrays.stream(calculAideDtoFields)
                        .filter(f -> f.getName().equals(data.getKey())).findFirst();

                if (field.isPresent()) {
                    field.get().setAccessible(true);

                    switch (field.get().getType().getName()) {
                        case "java.lang.Ineger":
                            field.get().set(calculAideDTO, Integer.valueOf(data.getValue()));
                            break;
                        case "java.math.BigDecimal":
                            field.get().set(calculAideDTO, ${artifactIdCamelCase}Utils.convertStringToBigDecimal((data.getValue())));
                            break;
                        default:
                            field.get().set(calculAideDTO, data.getValue());
                            break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

        });

        return calculAideDTO;
    }

    public SuiviComptableDTO getSuiviComptableDTO(final Integer demandeID) {
        SuiviComptableDTO suiviComptableDTO = new SuiviComptableDTO();
        Field[] suiviComptableDtoFields = SuiviComptableDTO.class.getDeclaredFields();
        List<DemandeDataDTO> demandesData = demandesDataService.getDemandeDatas(gouvPropertiesResolver.getDemarcheId(),
                demandeID);

        demandesData.stream().filter(d -> !StringUtils.isBlank(d.getValue())).forEach(data -> {
            try {

                Optional<Field> field = Arrays.stream(suiviComptableDtoFields)
                        .filter(f -> f.getName().equals(data.getKey())).findFirst();

                if (field.isPresent()) {
                    field.get().setAccessible(true);
                    field.get().set(suiviComptableDTO, data.getValue());
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

        });

        return suiviComptableDTO;
    }

    public void saveCalculAideDTO(final CalculAideDTO calculAideDTO, final Integer demandeID) {
        Field[] calculAideDtoFields = calculAideDTO.getClass().getDeclaredFields();
        Arrays.stream(calculAideDtoFields).filter(f -> !"serialVersionUID".equals(f.getName())).forEach(field -> {
            String value = "";
            try {
                field.setAccessible(true);

                if (field.get(calculAideDTO) != null) {
                    value = "java.math.BigDecimal".equals(field.getType().getName())
                            ? field.get(calculAideDTO).toString().replace(".", ",")
                            : field.get(calculAideDTO).toString();
                }

            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error(e.getMessage());
            }

            try {
                demandesDataService.saveOrUpdateDemandeData(gouvPropertiesResolver.getDemarcheId(), demandeID,
                        field.getName(), value);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        });
    }

    public void saveSuiviComptableDTO(final SuiviComptableDTO suiviComptableDTO, final Integer demandeID) {
        Field[] suiviComptableDtoFields = suiviComptableDTO.getClass().getDeclaredFields();

        Map<String, String> datas = new HashMap<>();
        Arrays.stream(suiviComptableDtoFields).filter(f -> !"serialVersionUID".equals(f.getName())).forEach(field -> {
            try {
                field.setAccessible(true);
                datas.put(field.getName(), field.get(suiviComptableDTO).toString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        });

        try {
            demandesDataService.saveOrUpdateDemandeDatas(gouvPropertiesResolver.getDemarcheId(), demandeID, datas);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

}
