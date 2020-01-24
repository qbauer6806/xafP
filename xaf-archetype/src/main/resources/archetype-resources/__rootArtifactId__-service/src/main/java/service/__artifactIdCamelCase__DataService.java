#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import ${groupId}.shared.dto.InformationsDetachementDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ${artifactIdCamelCase}DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(${artifactIdCamelCase}DataService.class);

    @Autowired
    private DemandesDataService demandesDataService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public InformationsDetachementDTO getInformationsDetachement(final Integer demandeID) {
        InformationsDetachementDTO infosDetachement = new InformationsDetachementDTO();

        Field[] infosDetachementFields = InformationsDetachementDTO.class.getDeclaredFields();
        List<DemandeDataDTO> demandesData = demandesDataService.getDemandeDatas(gouvPropertiesResolver.getDemarcheId(),
                demandeID);

        demandesData.stream().filter(d -> !StringUtils.isBlank(d.getValue())).forEach(data -> {
            try {

                Optional<Field> field = Arrays.stream(infosDetachementFields)
                        .filter(f -> f.getName().equals(data.getKey())).findFirst();

                if (field.isPresent()) {
                    field.get().setAccessible(true);

                    switch (field.get().getType().getName()) {
                        case "java.lang.Integer":
                            field.get().set(infosDetachement, Integer.valueOf(data.getValue()));
                            break;
                        default:
                            field.get().set(infosDetachement, data.getValue());
                            break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }

        });

        return infosDetachement;
    }

    public void saveInformationsDetachementDTO(final InformationsDetachementDTO informationsDetachementDTO, final Integer demandeID) {
        Field[] informationsDetachementDtoFields = informationsDetachementDTO.getClass().getDeclaredFields();
        Arrays.stream(informationsDetachementDtoFields).filter(f -> !"serialVersionUID".equals(f.getName())).forEach(field -> {
            String value = "";
            try {
                field.setAccessible(true);

                if (field.get(informationsDetachementDTO) != null) {
                    value = "java.lang.Integer".equals(field.getType().getName())
                            ? field.get(informationsDetachementDTO).toString().replace(".", ",")
                            : field.get(informationsDetachementDTO).toString();
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

}
