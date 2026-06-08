package mc.gouv.xaf.back.service.excel;

import java.util.Iterator;
import java.util.NoSuchElementException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class AfDemandeExcelFlatIterable implements Iterable<AfDemandeExcelFlatDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfDemandeExcelFlatIterable.class);

    private final DemandesService demandesService;
    private final ExcelRechercheDTO excelRechercheDTO;
    private final long total;
    private final int pageSize = 100;
    private int currentPage = 0;

    public AfDemandeExcelFlatIterable(DemandesService demandesService, ExcelRechercheDTO excelRechercheDTO,
            long total) {
        this.demandesService = demandesService;
        this.excelRechercheDTO = excelRechercheDTO;
        this.total = total;
    }

    @Override
    public Iterator<AfDemandeExcelFlatDTO> iterator() {
        return new Iterator<>() {

            private Iterator<AfDemandeExcelFlatDTO> currentIterator;

            @Override
            public boolean hasNext() {
                if (currentIterator == null || !currentIterator.hasNext()) {
                    loadNextPage();
                }
                return currentIterator != null && currentIterator.hasNext();
            }

            @Override
            public AfDemandeExcelFlatDTO next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return currentIterator.next();
            }

            private void loadNextPage() {
                Pageable pageRequest = PageRequest.of(currentPage++, pageSize,
                        Sort.by(Sort.Order.desc("dateCreation"), Sort.Order.desc("pkDemandes")));
                LOGGER.info("Export Excel - chargement de la page {} (taille={}, total={})",
                        pageRequest.getPageNumber(), pageSize, total);
                Page<AfDemandeExcelFlatDTO> page = demandesService.retrieveDemandesExcelPageable(pageRequest,
                        excelRechercheDTO, total
                );
                currentIterator = page.iterator();
            }
        };
    }
}

