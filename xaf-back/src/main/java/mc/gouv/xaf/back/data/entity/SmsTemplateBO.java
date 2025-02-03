package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM_SMS_TEMPLATES
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_SMS_TEMPLATES", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE", "LANGUE" }) })
public class SmsTemplateBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_SMSTEMPLATES", nullable = false)
    private Integer pkSmsTemplates;

    @Column(name = "CODE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String code;
    
    @Column(name = "SENDER", length = 11, nullable = true)
    @Size(min = 0, max = 11)
    private String sender;

    @Column(name = "CONTENU", length = 10000, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10000)
    private String contenu;

    @Column(name = "LANGUE", length = 2)
    @Size(max = 2)
    private String langue;

    @Column(name = "DATE_MODIF", nullable = false)
    private Date dateModif;

}
