package mc.gouv.xaf.servlet.dto;

import java.util.List;

public class FileSearchResultDTO {
    private String data;
    private String account;
    private String container;
    private String name;
    private List<FileMetaDTO> meta;
    private String dateCreation;
    private int size;
    private String contentType;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileMetaDTO> getMeta() {
        return meta;
    }

    public void setMeta(List<FileMetaDTO> meta) {
        this.meta = meta;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
