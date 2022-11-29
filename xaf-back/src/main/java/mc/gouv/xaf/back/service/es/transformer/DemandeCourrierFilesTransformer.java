package mc.gouv.xaf.back.service.es.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

public class DemandeCourrierFilesTransformer {

    private DemandeCourrierFilesTransformer() {
    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     *
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    public static List<DemandeFileDTO> recupererCourriersDemandeFromBO(Set<DemandesCourriersBO> courriers) {
        return recupererCourriersDemandeFromDTO(DemandesCourriersTransformer.bo2Dto(new ArrayList<>(courriers)));
    }

    /**
     * Méthode permettant transformer des DemandeCourrierDTO en DemandeFileDTO
     *
     * @param courriers courriers d'une demande
     * @return list des fichiers à ajouter
     */
    public static List<DemandeFileDTO> recupererCourriersDemandeFromDTO(List<DemandeCourrierDTO> courriers) {
        List<DemandeFileDTO> fichiers = new ArrayList<>();
        // Conversion DemandeCourrierBO en DemandeFileDTO pour faciliter l'indexation
        if (courriers != null) {
            for (DemandeCourrierDTO courrier : courriers) {
                DemandeFileDTO file = new DemandeFileDTO();
                file.setMeta(courrier.getMeta());
                file.setName(courrier.getName());
                file.setUrl(courrier.getUrl());
                file.setDate(courrier.getDateCreation());
                fichiers.add(file);
            }
        }
        return fichiers;
    }
	
}
