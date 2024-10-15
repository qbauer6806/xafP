package mc.gouv.xaf.back.data.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * 
 * @author qdeme
 *
 */
@Service
public class DemandesTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesTransformer.class);

    private static final String FIELD_COURRIER = "courriers";
    private static final String FIELD_FILES = "files";
    private static final String FIELD_STATUS = "statuts";
    private static final String FIELD_DEM_COMPL = "demandesComplements";
    private static final String FIELD_DATA = "data";

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private DemandesAgentsTransformer demandesAgentsTransformer;

    @Autowired
    private DemandesUsagersTransformer demandesUsagersTransformer;

    @Autowired
    private DemandesConfigTransformer demandesConfigTransformer;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private AfBackUtils afBackUtils;

    private DemandesTransformer() {
    }

    public DemandeDTO bo2Dto(DemandeBO bo) {
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
        if (fields == null) {
            return new boolean[] { true, true, true, true, true };
        }

        boolean[] addFields = new boolean[5];
        for (String field : fields) {
            switch (field) {
                case FIELD_COURRIER:
                    addFields[0] = true;
                    break;
                case FIELD_FILES:
                    addFields[1] = true;
                    break;
                case FIELD_STATUS:
                    addFields[2] = true;
                    break;
                case FIELD_DEM_COMPL:
                    addFields[3] = true;
                    break;
                case FIELD_DATA:
                    addFields[4] = true;
                    break;
                default:
                    break;
            }
        }
        return addFields;
    }

    private static boolean hasNonEmptyCollection(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public DemandeDTO bo2Dto(DemandeBO bo, String[] fields) {
        if (bo == null) {
            return null;
        }
        boolean[] addFields = getAllFields(fields);
        boolean addCourriersField = addFields[0] && hasNonEmptyCollection(bo.getCourriers());
        boolean addFilesField = addFields[1] && hasNonEmptyCollection(bo.getFiles());
        boolean addStatutsField = addFields[2] && hasNonEmptyCollection(bo.getStatuts());
        boolean addDemandesComplementsField = addFields[3] && hasNonEmptyCollection(bo.getDemandesComplements());
        boolean addDataField = addFields[4] && hasNonEmptyCollection(bo.getData());

        DemandeDTO dto = new DemandeDTO();
        dto.setFkAccess(bo.getFkAccess().getPkAccess());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setLangue(bo.getLangue());
        dto.setCanal(DemandeCanalEnum.valueOf(bo.getCanal()));
        dto.setObservations(bo.getObservations());
        dto.setPkDemandes(bo.getPkDemandes());
        dto.setCreeParAgentId(bo.getCreeParAgentId());
        if (!DemarchesUtils.isFrontUser()) {
            dto.setAgent(demandesAgentsTransformer.bo2Dto(bo.getAgent()));
        }
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setCourrierDateReception(bo.getCourrierDateReception());
        dto.setCourrierRefInterne(bo.getCourrierRefInterne());
        dto.setUsagerId(bo.getFkAccess().getUsagerId());
        dto.setUsager(demandesUsagersTransformer.bo2Dto(bo.getUsager()));
        dto.setRecapType(bo.getRecapType());
        dto.setPkDemandeSource(bo.getPkDemandeSource());
        dto.setModificationTimestamp(bo.getModificationTimestamp());

        if(bo.getTypeConnexionUsager() != null) {
            dto.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(bo.getTypeConnexionUsager()));
        }

        if(bo.getTypeConnexionUsager() != null) {
            dto.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(bo.getTypeConnexionUsager()));
        }

        // Mapper le contenu de la demande
        dto.setContenu(bo.getContenu());

        dto.setContenuTrad(bo.getContenuTrad());

        // Mapper le contenu de la config
        if (bo.getConfig() != null) {
            DemandeConfigBO config = bo.getConfig();
            dto.setConfig(demandesConfigTransformer.bo2Json(config));

            // mapper les marqueurs
            dto.setMarqueurs(
                    config.getMarqueurs().stream()
                            .collect(Collectors.toMap(
                                    MarqueurBO::getIdentifiant,
                                    marqueur -> afBackUtils.getMarqueurValue(bo.getContenuTrad(), marqueur.getChemin()),
                                    (existing, replacement) -> {
                                        // en cas de doublon d'identifiant, on utilise la 1ère valeur
                                        return existing;
                                    }
                            ))
            );
        }

        // Mapper les demandes d'informations complémentaires
        if (addDemandesComplementsField) {
            dto.setComplements(DemandesComplementsTransformer.bo2Dto(new ArrayList<>(bo.getDemandesComplements()))
                    .toArray(DemandeComplementsDTO[]::new));
        }

        // Mapper les fichiers
        if (addFilesField) {
            dto.setFichiers(DemandesFilesTransformer.bo2Dto(new ArrayList<>(bo.getFiles()))
                    .toArray(DemandeFileDTO[]::new));
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
                Map<String, String> privateStatus = demarchesDataProvider.getPrivateStatusMap();
                // si c'est un statut privé, alors on va chercher le dernier statut public pour l'afficher au FO
                if (privateStatus.get(statutDto.getName()) != null) {
                    List<DemandeStatutDTO> allStatus = DemandesStatutsTransformer.bo2Dto(new ArrayList<>(bo.getStatuts()));
                    allStatus.sort(Comparator.comparing(DemandeStatutDTO::getPkStatut).reversed());
                    for (DemandeStatutDTO demandeStatutDTO : allStatus) {
                        // si on tombe sur un statut public, on utilise celui-là
                        if (privateStatus.get(demandeStatutDTO.getName()) == null) {
                            statutDto.setName(demandeStatutDTO.getName());
                            statutDto.setLibelle(demandeStatutDTO.getLibelle());
                            break;
                        }
                    }
                    statutDto.setCodeMotif(null);
                }
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
                    .toArray(DemandeCourrierDTO[]::new));
        }

        // Mapper les données de demande
        if (addDataField) {
            dto.setData(DemandesDataTransformer.bo2Dto(new ArrayList<>(bo.getData()))
                    .toArray(DemandeDataDTO[]::new));
        }

        dto = bo2DtoProcessJsonFields(bo, dto);
        return dto;
    }

    private static DemandeDTO bo2DtoProcessJsonFields(DemandeBO bo, DemandeDTO dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
          // Mapper le contenu de la demande préremplie
            if (bo.getContenuInitial() != null)
                dto.setContenuInitial(mapper.readTree(bo.getContenuInitial()));

            // Meta
            if (bo.getMeta() != null)
            	dto.setMeta(mapper.readTree(bo.getMeta()));

            dto.setDonneesCertifiees(mapper.treeToValue(bo.getDonneesCertifiees(), SourceFiableDTO[].class));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        return dto;
    }

    private static DemandeDTO bo2DtoProcessStatuts(DemandeBO bo, DemandeDTO dto, boolean addStatutsField) {
        // Mapper les statuts
        if (addStatutsField && !DemarchesUtils.isFrontUser()) {
            dto.setStatuts(DemandesStatutsTransformer.bo2Dto(new ArrayList<>(bo.getStatuts())).toArray(DemandeStatutDTO[]::new));
        }
        return dto;
    }

    public List<DemandeDTO> bo2Dto(List<DemandeBO> bos, String[] fields) {
        ArrayList<DemandeDTO> dtos = new ArrayList<>();
        for (DemandeBO bo : bos) {
            dtos.add(bo2Dto(bo, fields));
        }
        return dtos;
    }

    public List<DemandeDTO> bo2Dto(List<DemandeBO> bos) {
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
    public DemandeBO dto2Bo(DemandeDTO dto) {
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
        if (dto.getAgent() != null) {
            User user = utilisateursCache.get(dto.getAgent().getId());
            bo.setAgent(demandesAgentsTransformer.user2Bo(user));
        }
        bo.setIdentifiant(dto.getIdentifiant());
        bo.setCourrierDateReception(dto.getCourrierDateReception());
        bo.setCourrierRefInterne(dto.getCourrierRefInterne());
        bo.setCreeParAgentId(dto.getCreeParAgentId());
        bo.setUsager(demandesUsagersTransformer.dto2Bo(dto.getUsager()));
        bo.setRecapType(dto.getRecapType());
        bo.setPkDemandeSource(dto.getPkDemandeSource());
        if(dto.getTypeConnexionUsager() != null) {
            bo.setTypeConnexionUsager(dto.getTypeConnexionUsager().name());
        }
        bo.setContenu(dto.getContenu());
        bo.setContenuTrad(dto.getContenuTrad());
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setMeta(mapper.writeValueAsString(dto.getMeta()));

            bo.setContenuInitial(mapper.writeValueAsString(dto.getContenuInitial()));
            // Ce qui suit afin d'éviter l'insertion d'une chaîne "null" en base
            if (bo.getContenuInitial() != null && "null".equals(bo.getContenuInitial())) {
            	bo.setContenuInitial(null);
            }
            bo.setDonneesCertifiees(mapper.valueToTree(dto.getDonneesCertifiees()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }

        return bo;
    }

    public mc.gouv.xaf.shared.dto.Page<DemandeDTO> boPage2DtoPage(Page<DemandeBO> bos) {
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
