package mc.gouv.xaf.back.data.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

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

    /**
     * <p>
     * Détermine quels champs vont être ajoutés à l'objet Demande.
     * </p>
     * <p>
     * Dans le cas où fields est null, on retourne un objet complet.
     * </p>
     */
    private static boolean[] getAllFields(String[] fields) {
        if (null == fields) {
            return new boolean[] { true, true, true, true, true };
        }

        boolean addCourriersField = false;
        boolean addFilesField = false;
        boolean addStatutsField = false;
        boolean addDemandesComplementsField = false;
        boolean addDataField = false;
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
        return new boolean[] { addCourriersField, addFilesField, addStatutsField, addDemandesComplementsField,
                addDataField };
    }

    public static DemandeDTO bo2Dto(DemandeBO bo, String[] fields) {
        if (bo == null) {
            return null;
        }

        boolean[] addFields = getAllFields(fields);
        boolean addCourriersField = addFields[0] && bo.getCourriers() != null && !bo.getCourriers().isEmpty();
        boolean addFilesField = addFields[1] && bo.getFiles() != null && !bo.getFiles().isEmpty();
        boolean addStatutsField = addFields[2] && bo.getStatuts() != null && !bo.getStatuts().isEmpty();
        boolean addDemandesComplementsField = addFields[3] && bo.getDemandesComplements() != null
                && !bo.getDemandesComplements().isEmpty();
        boolean addDataField = addFields[4] && bo.getData() != null && !bo.getData().isEmpty();

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
        dto.setAgentAffecteId(bo.getAgentAffecteId());
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setCourrierDateReception(bo.getCourrierDateReception());
        dto.setCourrierRefInterne(bo.getCourrierRefInterne());
        dto.setUsagerId(bo.getFkAccess().getUsagerId());
        dto.setUsagerNom(bo.getUsagerNom());
        dto.setUsagerPrenom(bo.getUsagerPrenom());
        dto.setUsagerEmail(bo.getUsagerEmail());
        dto.setBuildId(bo.getBuildId());
        dto.setRecapType(bo.getRecapType());
        dto.setDonneesCertifiees(bo.getDonneesCertifiees());
        dto.setPkDemandeSource(bo.getPkDemandeSource());
        dto.setModificationTimestamp(bo.getModificationTimestamp());

        if(bo.getTypeConnexionUsager() != null) {
            dto.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(bo.getTypeConnexionUsager()));
        }

        // Mapper les demandes d'informations complémentaires
        if (addDemandesComplementsField) {
            dto.setComplements(DemandesComplementsTransformer.bo2Dto(new ArrayList<>(bo.getDemandesComplements()))
                    .toArray(new DemandeComplementsDTO[bo.getDemandesComplements().size()]));
        }

        // Mapper les fichiers
        if (addFilesField) {
            dto.setFichiers(DemandesFilesTransformer.bo2Dto(new ArrayList<>(bo.getFiles()))
                    .toArray(new DemandeFileDTO[bo.getFiles().size()]));
        }

        // Mapper les statuts
        dto = bo2DtoProcessStatuts(bo, dto, addStatutsField);

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
        if (addCourriersField) {
            // Ticket https://redmine.monaco-gouvernement.mc/issues/25476
            // Avant le fix de ce ticket, on ne remontait pas les courriers à l'user FRONT
            // Donc lors de la création d'une demande courrier par l'API, les courriers ne sont pas indexés !
            // Cela marchait jusque là car on générait les tokens FO -> API de la mauvaise manière, avec rôle USER
            // au lieu de FRONT. Mais maintenant qu'on a user FRONT ça ne marche plus.
            // Décision prise de remonter les courriers dans les deux cas : FO (API) et BO
            // Car cela ne pose aucun problème de sécurité
            dto.setCourriers(DemandesCourriersTransformer.bo2Dto(new ArrayList<>(bo.getCourriers()))
                    .toArray(new DemandeCourrierDTO[bo.getCourriers().size()]));
        }

        // Mapper les données de demande
        if (addDataField) {
            dto.setData(DemandesDataTransformer.bo2Dto(new ArrayList<>(bo.getData()))
                    .toArray(new DemandeDataDTO[bo.getData().size()]));
        }

        dto = bo2DtoProcessJsonFields(bo, dto);

        return dto;
    }
    
    private static DemandeDTO bo2DtoProcessJsonFields(DemandeBO bo, DemandeDTO dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
        	// Mapper le contenu de la demande
            dto.setContenu(mapper.readTree(bo.getContenu()));
            
            // Mapper le contenu de la demande préremplie
            if (bo.getContenuInitial() != null)
                dto.setContenuInitial(mapper.readTree(bo.getContenuInitial()));
            
            // Meta
            if (bo.getMeta() != null)
            	dto.setMeta(mapper.readTree(bo.getMeta()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        return dto;
    }
    
    private static DemandeDTO bo2DtoProcessStatuts(DemandeBO bo, DemandeDTO dto, boolean addStatutsField) {
        // Mapper les statuts
        if (addStatutsField) {
            if (DemarchesUtils.isFrontUser()) {
                // Front Office : remonter uniquement le dernier statut de la demande
                DemandesStatutsBO statut = DemarchesUtils.getLatestStatus(bo);
                DemandeStatutDTO statutDto = DemandesStatutsTransformer.bo2Dto(statut);
                // Cacher l'agentId au Front Office
                statutDto.setAgentId(null);
                dto.setStatuts(new DemandeStatutDTO[] { statutDto });
            } else {
                // Back Office : tout remonter
                dto.setStatuts(DemandesStatutsTransformer.bo2Dto(new ArrayList<>(bo.getStatuts()))
                        .toArray(new DemandeStatutDTO[bo.getStatuts().size()]));
            }
        }
        return dto;
    }

    public static List<DemandeDTO> bo2Dto(List<DemandeBO> bos, String[] fields) {
        ArrayList<DemandeDTO> dtos = new ArrayList<>();
        for (DemandeBO bo : bos) {
            dtos.add(bo2Dto(bo, fields));
        }
        return dtos;
    }

    public static List<DemandeDTO> bo2Dto(List<DemandeBO> bos) {
        ArrayList<DemandeDTO> dtos = new ArrayList<>();
        for (DemandeBO bo : bos) {
            dtos.add(bo2Dto(bo, null));
        }
        return dtos;
    }

    /**
     * L'entité retournée est à rattacher à un AccessBO après l'appel à cette fonction Mapper les demandes
     * d'informations complémentaires attachées après l'appel à cette fonction, si besoin Mapper les fichiers attachés
     * après appel à cette fonction, si besoin Mapper les statuts attachés après appel à cette fonction, si besoin (y
     * compris le "dernier statut") Mapper les données de demande ("data") attachées après appel à cette fonction, si
     * besoin Mapper les courriers attachés après appel à cette fonction, si besoin
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
        bo.setDonneesCertifiees(dto.getDonneesCertifiees());
        bo.setPkDemandeSource(dto.getPkDemandeSource());
        if(dto.getTypeConnexionUsager() != null) {
            bo.setTypeConnexionUsager(dto.getTypeConnexionUsager().name());
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
            bo.setMeta(mapper.writeValueAsString(dto.getMeta()));
            
            bo.setContenuInitial(mapper.writeValueAsString(dto.getContenuInitial()));
            // Ce qui suit afin d'éviter l'insertion d'une chaîne "null" en base
            if (bo.getContenuInitial() != null && "null".equals(bo.getContenuInitial())) {
            	bo.setContenuInitial(null);
            }
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
