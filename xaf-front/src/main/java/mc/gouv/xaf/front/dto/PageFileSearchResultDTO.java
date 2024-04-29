package mc.gouv.xaf.front.dto;

import java.util.List;

public class PageFileSearchResultDTO {
    private Long totalElements;
    private Integer totalPages;
    private Integer size;
    private List<FileSearchResultDTO> content;
    private Integer number;
    private Sort sort;
    private Boolean first;
    private Boolean last;
    private Integer numberOfElements;
    private PageableObject pageable;
    private Boolean empty;

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<FileSearchResultDTO> getContent() {
        return content;
    }

    public void setContent(List<FileSearchResultDTO> content) {
        this.content = content;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public PageableObject getPageable() {
        return pageable;
    }

    public void setPageable(PageableObject pageable) {
        this.pageable = pageable;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }
}
