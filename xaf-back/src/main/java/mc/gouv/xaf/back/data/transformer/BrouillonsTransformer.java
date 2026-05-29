package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

/**
 * @author qdeme
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
        dto.setMeta(bo.getMeta());
        dto.setContenu(bo.getContenu());
        dto.setContenuInitial(bo.getContenuInitial());
        DemandeConfigBO demandeConfigBO = bo.getConfig();
        dto.setBuildId(demandeConfigBO != null ? demandeConfigBO.getBuildId() : null);
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
     * L'entité retournée est à rattacher à un AccessBO après l'appel à cette fonction Mapper les fichiers attachés
     * après appel à cette fonction, si besoin
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
        bo.setMeta(dto.getMeta());
        bo.setContenu(dto.getContenu());
        bo.setContenuInitial(dto.getContenuInitial());
        return bo;
    }

    public static mc.gouv.xaf.shared.dto.Page<BrouillonDTO> boPage2DtoPage(Page<BrouillonBO> bos) {
        mc.gouv.xaf.shared.dto.Page<BrouillonDTO> page = new mc.gouv.xaf.shared.dto.Page<>();
        page.setTotalElements(bos.getTotalElements());
        page.setNumber(bos.getNumber());
        page.setSize(bos.getSize());
        page.setNumberOfElements(bos.getNumberOfElements());
        page.setContent(bo2DtoPageLight(bos.getContent()));
        page.setTotalPages(bos.getTotalPages());
        page.setFirst(bos.isFirst());
        page.setLast(bos.isLast());
        page.setSort(bos.getSort());
        return page;
    }

    private static List<BrouillonDTO> bo2DtoPageLight(List<BrouillonBO> bos) {
        ArrayList<BrouillonDTO> dtos = new ArrayList<>();
        for (BrouillonBO bo : bos) {
            if (bo == null) {
                continue;
            }
            BrouillonDTO dto = new BrouillonDTO();
            dto.setFkAccess(bo.getFkAccess().getPkAccess());
            dto.setDateCreation(bo.getDateCreation());
            dto.setDateDerModif(bo.getDateDerModif());
            dto.setPkBrouillons(bo.getPkBrouillons());
            dto.setUsagerId(bo.getFkAccess().getUsagerId());
            DemandeConfigBO demandeConfigBO = bo.getConfig();
            dto.setBuildId(demandeConfigBO != null ? demandeConfigBO.getBuildId() : null);
            dto.setRecapType(bo.getRecapType());
            dtos.add(dto);
        }
        return dtos;
    }

    public static void setDernierStatut(BrouillonDTO brouillonDTO, String notTransmitted, String deprecated,
            String lastBuildId) {
        if (lastBuildId.equals(brouillonDTO.getBuildId())) {
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
