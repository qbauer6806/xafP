package mc.gouv.xaf.back.service.excel;

import java.util.Iterator;
import java.util.NoSuchElementException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AfDemandeExcelFlatIterable implements Iterable<AfDemandeExcelFlatDTO> {

    private final DemandesService demandesService;
    private final ExcelRechercheDTO excelRechercheDTO;
    private final long total;
    private final int pageSize = 200;
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
                Pageable pageRequest = PageRequest.of(currentPage++, pageSize);
                Page<AfDemandeExcelFlatDTO> page = demandesService.retrieveDemandesExcelPageable(pageRequest,
                        excelRechercheDTO, total
                );
                currentIterator = page.iterator();
            }
        };
    }
}

