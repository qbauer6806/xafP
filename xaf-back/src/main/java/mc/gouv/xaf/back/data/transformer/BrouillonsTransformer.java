package mc.gouv.xaf.back.data.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author qdeme
 *
 */
public class BrouillonsTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsTransformer.class);

    private BrouillonsTransformer() {
    }

    public static BrouillonDTO bo2Dto(BrouillonBO bo) {
        if (bo == null) {
            return null;
        }
        BrouillonDTO dto = new BrouillonDTO();
        dto.setFkAccess(bo.getFkAccess().getPkAccess());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setPkBrouillons(bo.getPkBrouillons());
        dto.setUsagerId(bo.getFkAccess().getUsagerId());
        // Mapper les fichiers
        if (bo.getFiles() != null && !bo.getFiles().isEmpty()) {
            dto.setFichiers(BrouillonsFilesTransformer.bo2Dto(new ArrayList<>(bo.getFiles()))
                    .toArray(new BrouillonFileDTO[bo.getFiles().size()]));
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setContenu(mapper.readTree(bo.getContenu()));
            if (bo.getMeta() != null) {
            	dto.setMeta(mapper.readTree(bo.getMeta()));
            }
            if (bo.getContenuInitial() != null)
                dto.setContenuInitial(mapper.readTree(bo.getContenuInitial()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        dto.setBuildId(bo.getConfig().getBuildId());
        dto.setDernierModele(bo.getConfig().isDernierModele());
        dto.setRecapType(bo.getRecapType());
        return dto;
    }

    public static List<BrouillonDTO> bo2Dto(List<BrouillonBO> bos) {
        ArrayList<BrouillonDTO> dtos = new ArrayList<>();
        for (BrouillonBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    /**
     * L'entité retournée est à rattacher à un AccessBO après l'appel à cette fonction
     * Mapper les fichiers attachés après appel à cette fonction, si besoin
     */
    public static BrouillonBO dto2Bo(BrouillonDTO dto) {
        if (dto == null) {
            return null;
        }
        BrouillonBO bo = new BrouillonBO();
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerModif(dto.getDateDerModif());
        bo.setPkBrouillons(dto.getPkBrouillons());
        bo.setRecapType(dto.getRecapType());
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
            if (dto.getMeta() != null) {
                bo.setMeta(mapper.writeValueAsString(dto.getMeta()));
            }
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

    public static mc.gouv.xaf.shared.dto.Page<BrouillonDTO> boPage2DtoPage(Page<BrouillonBO> bos) {
        mc.gouv.xaf.shared.dto.Page<BrouillonDTO> page = new mc.gouv.xaf.shared.dto.Page<>();
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

    public static void setDernierStatut(BrouillonDTO brouillonDTO, String notTransmitted, String deprecated) {
        if (brouillonDTO.isDernierModele()) {
            // statut not transmitted
            setDernierStatut(brouillonDTO, notTransmitted);
        } else {
            // statut deprecated
            setDernierStatut(brouillonDTO, deprecated);
        }
    }

    public static void setDernierStatut(BrouillonDTO brouillonDTO, String statut) {
        DemandeStatutDTO demandeStatutDTO = new DemandeStatutDTO();
        demandeStatutDTO.setName(statut);
        brouillonDTO.setDernierStatut(demandeStatutDTO);
    }

}
