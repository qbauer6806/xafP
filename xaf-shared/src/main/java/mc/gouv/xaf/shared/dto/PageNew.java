package mc.gouv.xaf.shared.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Copie de org.springframework.data.domain.Page pour ne pas avoir de dépendance avec Spring dans le client, et pour
 * matcher les besoins front Adaptation des noms pour la réponse de customRequest
 *
 * @param <T>
 * @author uek
 */
@Setter
@Getter
public class PageNew<T> {

    /**
     * -- GETTER --
     *
     * @return the total amount of elements
     */
    private long itemsCounter;

    /**
     * -- GETTER --
     *
     * @return the number of the current {@link org.springframework.data.domain.Slice}.
     */
    private int page;

    /**
     * -- GETTER --
     *
     * @return the size of the {@link org.springframework.data.domain.Slice}.
     */
    private int itemsPerPage;

    /**
     * -- GETTER --
     *
     * @return the page content as {@link List}.
     */
    private List<T> currentPageItems;

}
