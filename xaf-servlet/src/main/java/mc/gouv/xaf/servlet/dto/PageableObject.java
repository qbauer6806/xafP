package mc.gouv.xaf.servlet.dto;

public class PageableObject {
    private Long offset;
    private Sort sort;
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean unpaged;
    private Boolean paged;

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getUnpaged() {
        return unpaged;
    }

    public void setUnpaged(Boolean unpaged) {
        this.unpaged = unpaged;
    }

    public Boolean getPaged() {
        return paged;
    }

    public void setPaged(Boolean paged) {
        this.paged = paged;
    }
}
