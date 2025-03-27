package mc.gouv.xaf.back.service.excel;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.AfDemandeExcelFlatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AfDemandeExcelFlatIterable implements Iterable<AfDemandeExcelFlatDTO> {

    private static final String[] DATA = new String[] { "data" };
    private final DemandesService demandesService;
    private final AfExcelExportModelProvider excelExportModelProvider;
    private final DemandesTransformer demandesTransformer;
    private final Date startDate;
    private final Date endDate;
    private final int pageSize = 100;
    private int currentPage = 0;

    public AfDemandeExcelFlatIterable(DemandesService demandesService, AfExcelExportModelProvider excelExportModelProvider,
            DemandesTransformer demandesTransformer, Date startDate, Date endDate) {
        this.demandesService = demandesService;
        this.excelExportModelProvider = excelExportModelProvider;
        this.demandesTransformer = demandesTransformer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Iterator<AfDemandeExcelFlatDTO> iterator() {
        return new Iterator<>() {

            private Iterator<DemandeBO> currentIterator;

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
                return excelExportModelProvider.getDemandeFlat(demandesTransformer.bo2Dto(currentIterator.next(), DATA));
            }

            private void loadNextPage() {
                Pageable pageRequest = PageRequest.of(currentPage++, pageSize);
                Page<DemandeBO> page = demandesService.getAllDemandesFilteredByDate(pageRequest, startDate, endDate);
                currentIterator = page.iterator();
            }
        };
    }
}

