package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Copie de la classe org.springframework.data.domain.Page pour ne pas avoir de dépendance avec Spring dans le client
 *
 * @param <T>
 * @author fgaujous
 */
@Getter
public class Page<T> {

    /**
     * -- GETTER --
     *
     * @return the total amount of elements
     */
    @Setter
    private long totalElements;

    /**
     * -- GETTER --
     *
     * @return the number of the current page.
     */
    @Setter
    private int number;

    /**
     * -- GETTER --
     *
     * @return the number of elements in one page.
     */
    @Setter
    private int size;

    /**
     * -- GETTER --
     *
     * @return the number of elements in all of the pages.
     */
    @Setter
    private int numberOfElements;

    /**
     * -- GETTER --
     *
     * @return the page content as {@link List}.
     */
    @Setter
    private List<T> content;

    /**
     * -- GETTER --
     *
     * @return the number of total pages
     */
    @Setter
    private int totalPages;

    /**
     * -- GETTER --
     *
     * @return whether the current page is the first one.
     */
    @Setter
    private boolean first;

    /**
     * -- GETTER --
     *
     * @return whether the current page is the last one.
     */
    @Setter
    private boolean last;

    @JsonIgnore
    private Object sort;

    @JsonIgnore
    public void setSort(Object sort) {
        this.sort = sort;
    }

}
