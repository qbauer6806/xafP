package mc.gouv.xaf.back.service.excel;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AfDemandeExcelFlatIterable implements Iterable<AfDemandeExcelFlatDTO> {

    private final DemandesService demandesService;
    private final Date startDate;
    private final Date endDate;
    private final String statut;
    private final int pageSize = 200;
    private int currentPage = 0;

    public AfDemandeExcelFlatIterable(DemandesService demandesService, Date startDate, Date endDate, String statut) {
        this.demandesService = demandesService;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statut = statut;
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
                Page<AfDemandeExcelFlatDTO> page = demandesService.getAllDemandesFilteredByDateAndStatut(pageRequest,
                        startDate,
                        endDate, statut);
                currentIterator = page.iterator();
            }
        };
    }
}

