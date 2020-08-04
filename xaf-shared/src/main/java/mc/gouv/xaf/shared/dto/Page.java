package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Dupliquer de org.springframework.data.domain.Page pour ne pas avoir de dépendance avec Spring dans le client
 *
 * @param <T>
 * @author fgaujous
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
     * @return the total amount of elements
     */
    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return the number of the current {@link org.springframework.data.domain.Slice}.
     */
    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the size of the {@link org.springframework.data.domain.Slice}.
     */
    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return the number of elements currently on this {@link org.springframework.data.domain.Slice}.
     */
    public int getNumberOfElements() {
        return this.numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    /**
     * @return the page content as {@link List}.
     */
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    /**
     * @return the number of total pages
     */
    public int getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * @return whether the current {@link org.springframework.data.domain.Slice} is the first one.
     */
    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    /**
     * @return whether the current {@link org.springframework.data.domain.Slice} is the last one.
     */
    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public Object getSort() {
        return sort;
    }

    @JsonIgnore
    public void setSort(Object sort) {
        this.sort = sort;
    }

}
