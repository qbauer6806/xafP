package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageableObject {
    private Long offset;
    private Sort sort;
    private Integer pageNumber;
    private Integer pageSize;
    private Boolean unpaged;
    private Boolean paged;

}
