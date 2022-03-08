package mc.gouv.xaf.back.service.es.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.logon.shared.User;
import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.*;
import mc.gouv.xaf.back.data.es.model.*;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesStatutsTransformer;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Conditional(IndexationEnabledCondition.class)
public class DemandeEsTransformer {

    private static final String FIELD_COURRIER = "courriers";
    private static final String FIELD_STATUS = "statuts";
    private static final String FIELD_DEM_COMPL = "demandesComplements";
    private static final String FIELD_DATA = "data";

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeEsTransformer.class);

    @Inject
    @Lazy
    UsagersCache usagersCache;

    @Inject
    private UtilisateursCache utilisateursCache;

    @Autowired(required = false)
    IndexedDemandeJsonNodeTransformer indexedDemandeJsonNodeTransformer;

    @Autowired(required = false)
    IndexedDemandeDataJsonNodeTransformer indexedDemandeDataJsonNodeTransformer;

    @Inject
    MotifsCache motifsCache;

    @Inject
    DemandesStatutsEsTransformer demandesStatutsEsTransformer;

    @Inject
    DemarchesDataProvider demarchesDataProvider;

    @Inject
    private DemandesHistoriqueService demandesHistoriqueService;

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    private DemandeEsTransformer() {
    }

    public Page<DemandeEsDTO> toEs(Page<DemandeBO> demandes) throws IOException {

        if (demandes == null) {
            return null;
        }

        List<DemandeEsDTO> demandesEs = new ArrayList<>();

        for (DemandeBO dem : demandes) {
            demandesEs.add(toEs(dem));
        }

        return new PageImpl<>(demandesEs);

    }

    public DemandeEsDTO toEs(DemandeBO demande) throws IOException {

        if (demande == null) {
            return null;
        }

        DemandeEsDTO demandeEsDTO = new DemandeEsDTO();
        demandeEsDTO.setDemandeJoinField(new DemandeJoinFieldEsDTO("demandes"));
        DemandeAccessEsDTO demandeAccessEsDto = new DemandeAccessEsDTO();

        AccessBO accessBO = demande.getFkAccess();
        demandeAccessEsDto.setUsagerId(accessBO.getUsagerId());
        demandeAccessEsDto.setDemarcheId(accessBO.getDemarcheId());
        demandeAccessEsDto.setFkAccess(accessBO.getPkAccess());
        demandeAccessEsDto.setActive(accessBO.isActive());

        demandeEsDTO.setAccess(demandeAccessEsDto);

        if (accessBO.getUsagerId() != null) {
            UsagerBean usagerBean = usagersCache.get(accessBO.getUsagerId());
            demandeEsDTO.setUsager(UsagerTransformer.bo2Dto(usagerBean));
        }

        if (demande.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(demande.getAgentAffecteId());
            demandeEsDTO.setAgent(AgentEsTransformer.bo2Dto(user));
        }

        CanalEsDto canal = new CanalEsDto();
        canal.setCode(DemandeCanalEnum.valueOf(demande.getCanal()).name());
        canal.setLibelle(DemandeCanalEnum.valueOf(demande.getCanal()).libelle);
        demandeEsDTO.setCanal(canal);

        if (StringUtils.equals(DemandeCanalEnum.GUICHET_VIRTUEL.name(), canal.getCode())) {
            demandeEsDTO.setDateDemande(demande.getDateCreation());
        } else {
            demandeEsDTO.setDateDemande(demande.getCourrierDateReception());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode contenu = mapper.readTree(demande.getContenu());
        demandeEsDTO.setContenu(transformContenu(contenu, demande.getBuildId()));
        demandeEsDTO.setCourrierDateReception(demande.getCourrierDateReception());
        demandeEsDTO.setCourrierRefInterne(demande.getCourrierRefInterne());

        demandeEsDTO.setStatutPublicOuInterne(demarchesDataProvider.getStatutPublicOuInterne(demande.getPkDemandes(), demande.getDernierStatut().getLibelle()).getName());

        Set<DemandeCourrierDTO> courriers = DemandesCourriersTransformer.bo2Dto(demande.getCourriers());

        if (courriers != null && !courriers.isEmpty()) {
            List<String> nomsCourriers = courriers.stream().map(DemandeCourrierDTO::getName)
                    .collect(Collectors.toList());
            demandeEsDTO.setNomsCourriers(nomsCourriers);
        }
        demandeEsDTO.setAgentAffecteId(demande.getAgentAffecteId());

        // Mapping des demandes data
        demandeEsDTO.setData(transformDataBO(demande.getData()));

        demandeEsDTO.setDateCreation(demande.getDateCreation());
        demandeEsDTO.setDateDerModif(demande.getDateDerModif());
        demandeEsDTO.setDernierStatut(
                demandesStatutsEsTransformer.bo2Dto(demande.getDernierStatut(), demande.getPkDemandes()));

        demandeEsDTO.setIdentifiant(demande.getIdentifiant());
        demandeEsDTO.setLangue(demande.getLangue());
        demandeEsDTO.setObservations(demande.getObservations());
        demandeEsDTO.setPkDemandes(demande.getPkDemandes());
        Set<DemandeStatutEsDTO> statuts = demandesStatutsEsTransformer.bo2Dto(demande.getStatuts(),
                demande.getPkDemandes());
        demandeEsDTO.setStatuts(statuts.toArray(new DemandeStatutEsDTO[statuts.size()]));

        if (demande.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(demande.getAgentAffecteId());
            demandeEsDTO.setAgentAffecteNomAffichage(getAgentAffecteNomAffichage(user));
        }

        return demandeEsDTO;
    }

    public DemandeEsDTO toEs(DemandeDTO demandeDTO, Boolean activeAccess) {
        if (demandeDTO == null) {
            return null;
        }

        DemandeEsDTO demandeEsDTO = new DemandeEsDTO();
        DemandeAccessEsDTO demandeAccessEsDto = new DemandeAccessEsDTO();
        demandeAccessEsDto.setUsagerId(demandeDTO.getUsagerId());
        demandeAccessEsDto.setDemarcheId(demandeDTO.getDemarcheId());
        demandeAccessEsDto.setFkAccess(demandeDTO.getFkAccess());
        demandeAccessEsDto.setActive(activeAccess);
        demandeEsDTO.setAccess(demandeAccessEsDto);
        if (demandeDTO.getUsagerId() != null) {
            UsagerBean usagerBean = usagersCache.get(demandeDTO.getUsagerId());
            demandeEsDTO.setUsager(UsagerTransformer.bo2Dto(usagerBean));
        }
        if (demandeDTO.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(demandeDTO.getAgentAffecteId());
            demandeEsDTO.setAgent(AgentEsTransformer.bo2Dto(user));
        }

        demandeEsDTO.setDateDemande(demandeDTO.getDateCreation());
        CanalEsDto canal = new CanalEsDto();
        if (demandeDTO.getCanal() != null) {
            canal.setCode(demandeDTO.getCanal().name());
            canal.setLibelle(demandeDTO.getCanal().libelle);
            demandeEsDTO.setCanal(canal);
            if (!StringUtils.equals(DemandeCanalEnum.GUICHET_VIRTUEL.name(), canal.getCode())) {
                demandeEsDTO.setDateDemande(demandeDTO.getCourrierDateReception());
            }
        }

        demandeEsDTO.setContenu(transformContenu(demandeDTO.getContenu(), demandeDTO.getBuildId()));
        demandeEsDTO.setCourrierDateReception(demandeDTO.getCourrierDateReception());
        demandeEsDTO.setCourrierRefInterne(demandeDTO.getCourrierRefInterne());

        demandeEsDTO.setStatutPublicOuInterne(demarchesDataProvider.getStatutPublicOuInterne(demandeDTO.getPkDemandes(), demandeDTO.getDernierStatut().getLibelle()).getName());

        if (demandeDTO.getCourriers() != null && demandeDTO.getCourriers().length > 0) {
            List<String> nomsCourriers = Arrays.stream(demandeDTO.getCourriers()).map(DemandeCourrierDTO::getName)
                    .collect(Collectors.toList());
            demandeEsDTO.setNomsCourriers(nomsCourriers);
        }

        demandeEsDTO.setAgentAffecteId(demandeDTO.getAgentAffecteId());

        // Mapping des demandes data
        demandeEsDTO.setData(transformDataDTO(demandeDTO.getData()));

        demandeEsDTO.setDateCreation(demandeDTO.getDateCreation());
        demandeEsDTO.setDateDerModif(demandeDTO.getDateDerModif());
        demandeEsDTO.setDernierStatut(
                demandesStatutsEsTransformer.toEs(demandeDTO.getDernierStatut(), demandeDTO.getPkDemandes()));

        demandeEsDTO.setIdentifiant(demandeDTO.getIdentifiant());
        demandeEsDTO.setLangue(demandeDTO.getLangue());
        demandeEsDTO.setObservations(demandeDTO.getObservations());
        demandeEsDTO.setPkDemandes(demandeDTO.getPkDemandes());
        demandeEsDTO.setStatuts(demandesStatutsEsTransformer.toEs(demandeDTO.getStatuts(), demandeDTO.getPkDemandes()));
        demandeEsDTO.setUpdated(demandeDTO.isUpdated());

        if (demandeEsDTO.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(demandeDTO.getAgentAffecteId());
            demandeEsDTO.setAgentAffecteNomAffichage(getAgentAffecteNomAffichage(user));
        }

        // Justificatifs de traitment dans l'historique de la demande
        List<String> justifs = getJustificatifsTraitement(gouvPropertiesResolver.getDemarcheId(),
                demandeDTO.getPkDemandes());
        demandeEsDTO.setJustificatifsTraitement(justifs);

        return demandeEsDTO;
    }

    public DemandeEsDTO toEs(DemandeDTO demandeDTO) {

        return toEs(demandeDTO, null);

    }

    public DemandeEsDTO bo2Dto(DemandeBO bo, String[] fields) {
        if (bo == null) {
            return null;
        }

        boolean addCourriersField = false;
        boolean addStatutsField = false;
        boolean addDemandesComplementsField = false;
        boolean addDataField = false;
        if (fields != null) {
            for (String field : fields) {
                if (StringUtils.equals(FIELD_COURRIER, field)) {
                    addCourriersField = true;
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
            // Dans le cas ou il n'y avait pas de fields on retourne l'objet complet
            addCourriersField = true;
            addStatutsField = true;
            addDemandesComplementsField = true;
            addDataField = true;
        }
        DemandeEsDTO dto = new DemandeEsDTO();
        DemandeAccessEsDTO acess = new DemandeAccessEsDTO();
        acess.setActive(bo.getFkAccess().isActive());
        acess.setDemarcheId(bo.getFkAccess().getDemarcheId());
        acess.setFkAccess(bo.getFkAccess().getPkAccess());
        acess.setUsagerId(bo.getFkAccess().getUsagerId());
        dto.setAccess(acess);
        if (acess.getUsagerId() != null) {
            UsagerBean usagerBean = usagersCache.get(acess.getUsagerId());
            dto.setUsager(UsagerTransformer.bo2Dto(usagerBean));
        }

        if (bo.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(bo.getAgentAffecteId());
            dto.setAgent(AgentEsTransformer.bo2Dto(user));
        }
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setLangue(bo.getLangue());

        dto.setDateDemande(bo.getDateCreation());
        CanalEsDto canal = new CanalEsDto();
        if (bo.getCanal() != null) {
            canal.setCode(DemandeCanalEnum.valueOf(bo.getCanal()).name());
            canal.setLibelle(DemandeCanalEnum.valueOf(bo.getCanal()).libelle);
            dto.setCanal(canal);
            if (!StringUtils.equals(DemandeCanalEnum.GUICHET_VIRTUEL.name(), canal.getCode())) {
                dto.setDateDemande(bo.getCourrierDateReception());
            }
        }

        dto.setObservations(bo.getObservations());
        dto.setPkDemandes(bo.getPkDemandes());
        dto.setAgentAffecteId(bo.getAgentAffecteId());

        if (bo.getAgentAffecteId() != null) {
            User user = utilisateursCache.get(bo.getAgentAffecteId());
            dto.setAgentAffecteNomAffichage(getAgentAffecteNomAffichage(user));
        }

        // Mapper les statuts
        if (addStatutsField && bo.getStatuts() != null && !bo.getStatuts().isEmpty()) {
            if (DemarchesUtils.isFrontUser()) {
                // Front Office : remonter uniquement le dernier statut de la demande
                DemandesStatutsBO statut = DemarchesUtils.getLatestStatus(bo);
                DemandeStatutDTO statutDto = DemandesStatutsTransformer.bo2Dto(statut);
                // Cacher l'agentId au Front Office
                statutDto.setAgentId(null);
                dto.setStatuts(new DemandeStatutEsDTO[] { demandesStatutsEsTransformer.toEs(statutDto, bo.getPkDemandes()) });
            } else {
                // Back Office : tout remonter
                Set<DemandeStatutEsDTO> statuts = demandesStatutsEsTransformer.bo2Dto(bo.getStatuts(),
                        bo.getPkDemandes());
                dto.setStatuts(statuts.toArray(new DemandeStatutEsDTO[statuts.size()]));
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
            dto.setDernierStatut(demandesStatutsEsTransformer.toEs(statutDto, bo.getPkDemandes()));
        }
        // Mapper les courriers
        if (addCourriersField && bo.getCourriers() != null && !bo.getCourriers().isEmpty()) {
            // Ne remonter les courriers que pour le back
            if (!DemarchesUtils.isFrontUser()) {
                // Back Office : tout remonter

                List<DemandeCourrierDTO> courriers = DemandesCourriersTransformer
                        .bo2Dto(new ArrayList<DemandesCourriersBO>(bo.getCourriers()));

                if (courriers != null && !courriers.isEmpty()) {
                    List<String> nomsCourriers = courriers.stream().map(DemandeCourrierDTO::getName)
                            .collect(Collectors.toList());
                    dto.setNomsCourriers(nomsCourriers);
                }

            }
        }
        dto.setIdentifiant(bo.getIdentifiant());
        dto.setCourrierDateReception(bo.getCourrierDateReception());
        dto.setCourrierRefInterne(bo.getCourrierRefInterne());

        dto.setStatutPublicOuInterne(demarchesDataProvider.getStatutPublicOuInterne(bo.getPkDemandes(), bo.getDernierStatut().getLibelle()).getName());

        // Mapping des demandes data
        dto.setData(transformDataBO(bo.getData()));

        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setContenu(transformContenu(mapper.readTree(bo.getContenu()), bo.getBuildId()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }

        // Justificatifs de traitment dans l'historique de la demande
        List<String> justifs = getJustificatifsTraitement(gouvPropertiesResolver.getDemarcheId(), bo.getPkDemandes());
        dto.setJustificatifsTraitement(justifs);

        return dto;
    }

    private String getAgentAffecteNomAffichage(User user) {
        if (user != null) {
            char prenom = ' ';
            if (user.getPrenom() != null && user.getPrenom().length() > 1) {
                prenom = user.getPrenom().charAt(0);
            }

            String nom = "";

            if (user.getNomAffichage() != null) {
                nom = user.getNomAffichage();
            }

            return prenom + "." + nom;
        }
        return null;
    }

    private GenericContenuEsDTO transformContenu(JsonNode node, String buildId) {
        if (indexedDemandeJsonNodeTransformer != null) {
            return indexedDemandeJsonNodeTransformer.buildGenericContenu(node, buildId);
        }
        return null;
    }

    private GenericDemandeDataEsDTO transformDataBO(Set<DemandesDataBO> dataBOS) {
        if (indexedDemandeDataJsonNodeTransformer != null) {
            return indexedDemandeDataJsonNodeTransformer.buildDemandeDataBO(dataBOS);
        }
        return null;
    }

    private GenericDemandeDataEsDTO transformDataDTO(DemandeDataDTO[] dataDTOS) {
        if (dataDTOS != null && indexedDemandeDataJsonNodeTransformer != null) {
            return indexedDemandeDataJsonNodeTransformer.buildDemandeDataDTO(dataDTOS);
        }
        return null;
    }

    /**
     * Méthode qui récupère la liste des justificatifs de traitement
     */
    private List<String> getJustificatifsTraitement(String demarcheId, Integer demandeId) {
        List<String> justifs = new ArrayList<>();
        List<DemandeHistoriqueDTO> histosDem = demandesHistoriqueService.getHistorique(demarcheId, demandeId);
        if (histosDem != null && !histosDem.isEmpty()) {
            justifs = histosDem.stream().map(DemandeHistoriqueDTO::getJustificatifTraitement).collect(Collectors.toList());
        }
        return justifs;
    }

}
