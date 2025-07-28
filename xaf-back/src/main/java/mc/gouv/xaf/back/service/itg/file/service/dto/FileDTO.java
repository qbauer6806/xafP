package mc.gouv.xaf.back.service.itg.file.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

/**
 * DTO représentant un fichier
 * 
 * @author qdeme
 *
 */
@Data
public class FileDTO {

    private InputStream data;

    private String account;

    private String container;

    private String name;

    private Set<MetaDTO> meta;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    private long size;
    
    private String contentType;

}
