package mc.gouv.xaf.back.shared.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Dupliquer de org.springframework.data.domain.Page pour ne pas avoir de dépendance avec Spring dans le client
 * @author fgaujous
 *
 * @param <T>
 */
public class Page<T> {

    private long totalElements;

    private int number;

    private int size;

    private int numberOfElements;

    private List<T> content;

    private int totalPages;

    private boolean first;

    private boolean last;

    @JsonIgnore
    private Object sort;

    /**
     * Returns the number of the current {@link Slice}. Is always non-negative.
     * 
     * @return the number of the current {@link Slice}.
     */
    public int getNumber() {
        return this.number;
    }

    /**
     * Returns the size of the {@link Slice}.
     * 
     * @return the size of the {@link Slice}.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the number of elements currently on this {@link Slice}.
     * 
     * @return the number of elements currently on this {@link Slice}.
     */
    public int getNumberOfElements() {
        return this.numberOfElements;
    }

    /**
     * Returns the page content as {@link List}.
     * 
     * @return
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Returns the number of total pages.
     * 
     * @return the number of total pages
     */
    public int getTotalPages() {
        return this.totalPages;
    }

    /**
     * Returns the total amount of elements.
     * 
     * @return the total amount of elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Returns whether the current {@link Slice} is the first one.
     * 
     * @return
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Returns whether the current {@link Slice} is the last one.
     * 
     * @return
     */
    public boolean isLast() {
        return last;
    }

    public Object getSort() {
        return sort;
    }

    @JsonIgnore
    public void setSort(Object sort) {
        this.sort = sort;
    }

}
