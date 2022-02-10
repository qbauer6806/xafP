package mc.gouv.xaf.back.data.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import org.springframework.data.domain.Page;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesTransformer.class);

    private static final String FIELD_COURRIER = "courriers";
    private static final String FIELD_FILES = "files";
    private static final String FIELD_STATUS = "statuts";
    private static final String FIELD_DEM_COMPL = "demandesComplements";
    private static final String FIELD_DATA = "data";

    private DemandesTransformer() {
    }

    public static DemandeDTO bo2Dto(DemandeBO bo) {
        return bo2Dto(bo, null);
    }

    public static DemandeDTO bo2Dto(DemandeBO bo, String[] fields) {
        if (bo == null) {
            return null;
        }

        boolean addCourriersField = false;
        boolean addFilesField = false;
        boolean addStatutsField = false;
        boolean addDemandesComplementsField = false;
        boolean addDataField = false;
        if (fields != null) {
            for (String field : fields) {
                if (StringUtils.equals(FIELD_COURRIER, field)) {
                    addCourriersField = true;
                }
                if (StringUtils.equals(FIELD_FILES, field)) {
                    addFilesField = true;
                }
                if (StringUtils.equals(FIELD_STATUS, field)) {
                    addStatutsField = true;
                }
                if (StringUtils.equals(FIELD_DEM_COMPL, field)) {
                    addDemandesComplementsField = true;
                }
                if (StringUtils.equals(FIELD_DATA, field)) {
                    addDataField = true;
                }
            }
        } else {
            //Dans le cas ou il n'y avait pas de fields on retourne l'objet complet
            addCourriersField = true;
            addFilesField = true;
            addStatutsField = true;
            addDemandesComplementsField = true;
            addDataField = true;
        }
        DemandeDTO dto = new DemandeDTO();
        dto.setFkAccess(bo.getFkAccess().getPkAccess());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setLangue(bo.getLangue());
        dto.setCanal(DemandeCanalEnum.valueOf(bo.getCanal()));
        dto.setObservations(bo.getObservations());
        dto.setDemarcheId(bo.getFkAccess().getDemarcheId());
        dto.setPkDemandes(bo.getPkDemandes());
        dto.setCreeParAgentId(bo.getCreeParAgentId());
        // Mapper les demandes d'informations complémentaires
        if (addDemandesComplementsField && bo.getDemandesComplements() != null
                && bo.getDemandesComplements().size() > 0) {
            dto.setComplements(DemandesComplementsTransformer
                    .bo2Dto(new ArrayList<DemandesComplementsBO>(bo.getDemandesComplements()))
                    .toArray(new DemandeComplementsDTO[bo.getDemandesComplements().size()]));
        }
        // Mapper les fichiers
        if (addFilesField && bo.getFiles() != null && bo.getFiles().size() > 0) {
            dto.setFichiers(DemandesFilesTransformer.bo2Dto(new ArrayList<DemandesFilesBO>(bo.getFiles()))
                    .toArray(new DemandeFileDTO[bo.getFiles().size()]));
        }
        // Mapper les statuts
        if (addStatutsField && bo.getStatuts() != null && bo.getStatuts().size() > 0) {
            if (DemarchesUtils.isFrontUser()) {
                // Front Office : remonter uniquement le dernier statut de la demande
                DemandesStatutsBO statut = DemarchesUtils.getLatestStatus(bo);
                DemandeStatutDTO statutDto = DemandesStatutsTransformer.bo2Dto(statut);
                // Cacher l'agentId au Front Office
                statutDto.setAgentId(null);
                dto.setStatuts(new DemandeStatutDTO[] { statutDto });
            } else {
                // Back Office : tout remonter
                dto.setStatuts(DemandesStatutsTransformer.bo2Dto(new ArrayList<DemandesStatutsBO>(bo.getStatuts()))
                        .toArray(new DemandeStatutDTO[bo.getStatuts().size()]));
            }
        }
        // Mapper le "dernier statut"
        if (bo.getDernierStatut() != null) {
            DemandesStatutsBO statut = bo.getDernierStatut();
            DemandeStatutDTO statutDto = DemandesStatutsTransformer.bo2Dto(statut);
            if (DemarchesUtils.isFrontUser()) {
                // Cacher l'agentId au Front Office
                statutDto.setAgentId(null);
            }
            dto.setDernierStatut(statutDto);
        }
        // Mapper les courriers
        if (addCourriersField && bo.getCourriers() != null && bo.getCourriers().size() > 0) {
        	// Ticket https://redmine.monaco-gouvernement.mc/issues/25476
        	// Avant le fix de ce ticket, on ne remontait pas les courriers à l'user FRONT
        	// Donc lors de la création d'une demande courrier par l'API, les courriers ne sont pas indexés !
        	// Cela marchait jusque là car on générait les tokens FO -> API de la mauvaise manière, avec rôle USER
        	// au lieu de FRONT. Mais maintenant qu'on a user FRONT ça ne marche plus.
        	// Décision prise de remonter les courriers dans les deux cas : FO (API) et BO
        	// Car cela ne pose aucun problème de sécurité
            dto.setCourriers(
                    DemandesCourriersTransformer.bo2Dto(new ArrayList<DemandesCourriersBO>(bo.getCourriers()))
                            .toArray(new DemandeCourrierDTO[bo.getCourriers().size()]));
        }
        dto.setAgentAffecteId(bo.getAgentAffecteId());
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setCourrierDateReception(bo.getCourrierDateReception());
        dto.setCourrierRefInterne(bo.getCourrierRefInterne());

        // Mapper les données de demande
        if (addDataField && bo.getData() != null && bo.getData().size() > 0) {
            dto.setData(DemandesDataTransformer.bo2Dto(new ArrayList<DemandesDataBO>(bo.getData()))
                    .toArray(new DemandeDataDTO[bo.getData().size()]));
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setContenu(mapper.readTree(bo.getContenu()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        dto.setUsagerId(bo.getFkAccess().getUsagerId());
        dto.setUsagerNom(bo.getUsagerNom());
        dto.setUsagerPrenom(bo.getUsagerPrenom());
        dto.setUsagerEmail(bo.getUsagerEmail());
        dto.setBuildId(bo.getBuildId());
        dto.setRecapType(bo.getRecapType());
        return dto;
    }

    public static List<DemandeDTO> bo2Dto(List<DemandeBO> bos, String[] fields) {
        ArrayList<DemandeDTO> dtos = new ArrayList<DemandeDTO>();
        for (DemandeBO bo : bos) {
            dtos.add(bo2Dto(bo, fields));
        }
        return dtos;
    }

    public static List<DemandeDTO> bo2Dto(List<DemandeBO> bos) {
        ArrayList<DemandeDTO> dtos = new ArrayList<DemandeDTO>();
        for (DemandeBO bo : bos) {
            dtos.add(bo2Dto(bo, null));
        }
        return dtos;
    }

    /**
     * L'entité retournée est à rattacher à un AccessBO après l'appel à cette fonction
     * Mapper les demandes d'informations complémentaires attachées après l'appel à cette fonction, si besoin
     * Mapper les fichiers attachés après appel à cette fonction, si besoin
     * Mapper les statuts attachés après appel à cette fonction, si besoin (y compris le "dernier statut")
     * Mapper les données de demande ("data") attachées après appel à cette fonction, si besoin
     * Mapper les courriers attachés après appel à cette fonction, si besoin
     * @param dto
     * @return
     */
    public static DemandeBO dto2Bo(DemandeDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandeBO bo = new DemandeBO();
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerModif(dto.getDateDerModif());
        bo.setLangue(dto.getLangue());
        bo.setCanal(dto.getCanal().name());
        bo.setObservations(dto.getObservations());
        bo.setPkDemandes(dto.getPkDemandes());
        bo.setAgentAffecteId(dto.getAgentAffecteId());
        bo.setIdentifiant(dto.getIdentifiant());
        bo.setCourrierDateReception(dto.getCourrierDateReception());
        bo.setCourrierRefInterne(dto.getCourrierRefInterne());
        bo.setCreeParAgentId(dto.getCreeParAgentId());
        bo.setUsagerNom(dto.getUsagerNom());
        bo.setUsagerPrenom(dto.getUsagerPrenom());
        bo.setUsagerEmail(dto.getUsagerEmail());
        bo.setBuildId(dto.getBuildId());
        bo.setRecapType(dto.getRecapType());
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        return bo;
    }

    public static mc.gouv.xaf.shared.dto.Page<DemandeDTO> boPage2DtoPage(Page<DemandeBO> bos) {
        mc.gouv.xaf.shared.dto.Page<DemandeDTO> page = new mc.gouv.xaf.shared.dto.Page<>();
        page.setTotalElements(bos.getTotalElements());
        page.setNumber(bos.getNumber());
        page.setSize(bos.getSize());
        page.setNumberOfElements(bos.getNumberOfElements());
        page.setContent(bo2Dto(bos.getContent()));
        page.setTotalPages(bos.getTotalPages());
        page.setFirst(bos.isFirst());
        page.setLast(bos.isLast());
        page.setSort(bos.getSort());
        return page;
    }

}
