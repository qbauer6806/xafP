package mc.gouv.xaf.front.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class FileSearchResultDTO {
    @Setter
    @Getter
    private String data;
    @Setter
    @Getter
    private String account;
    @Setter
    @Getter
    private String container;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private List<FileMetaDTO> meta;
    @Setter
    @Getter
    private String dateCreation;
    private int size;
    @Setter
    @Getter
    private String contentType;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
