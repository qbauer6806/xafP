package mc.gouv.xaf.back.service.expiration.settings;

import java.io.Serializable;

/**
 * Classe permettant de créer des conf de statut à expirer en spécifiant
 * - Le statut à expirer
 * - La clé liée à la propriété du délai d'expiration
 * - Le prefix de la clef mail à utiliser (ie MAIL_EN_ATTENT_COMPL)
 *
 * @author XDECOOL.EXT
 */
public class ExpirationStatutDemandeConf implements Serializable {

    private static final long serialVersionUID = 6972884327194347877L;
    private String statutAExpirer;
    private String cleDelaiExpiration;
    private String clefMailPrefix;

    public ExpirationStatutDemandeConf(String statutAExpirer, String cleDelaiExpiration, String clefMailPrefix) {
        this.statutAExpirer = statutAExpirer;
        this.cleDelaiExpiration = cleDelaiExpiration;
        this.clefMailPrefix = clefMailPrefix;
    }

    public String getStatutAExpirer() {
        return statutAExpirer;
    }

    public void setStatutAExpirer(String statutAExpirer) {
        this.statutAExpirer = statutAExpirer;
    }

    public String getCleDelaiExpiration() {
        return cleDelaiExpiration;
    }

    public void setCleDelaiExpiration(String cleDelaiExpiration) {
        this.cleDelaiExpiration = cleDelaiExpiration;
    }

    public String getClefMailPrefix() {
        return clefMailPrefix;
    }

    public void setClefMailPrefix(String clefMailPrefix) {
        this.clefMailPrefix = clefMailPrefix;
    }

}
