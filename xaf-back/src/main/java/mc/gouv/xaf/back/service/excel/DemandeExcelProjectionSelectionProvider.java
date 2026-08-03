package mc.gouv.xaf.back.service.excel;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;

/**
 * Point d'extension permettant à une TS de fournir les sélections Criteria spécifiques
 * et le mapping associé pour la projection Excel légère.
 */
public interface DemandeExcelProjectionSelectionProvider {

    List<Selection<?>> buildSelections(Root<DemandeBO> root, CriteriaBuilder cb);

    AfDemandeExcelFlatDTO mapTuple(Tuple tuple, AfBackUtils afBackUtils);
}
