package mc.gouv.xaf.back.service.relance.settings;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * Classe permettant de créer des conf de statut à expirer en spécifiant
 * - Le statut à relancer
 * - La clé de la propriété du délai avant la 1ere relance
 * - La clé de la propriété du délai entre 2 relances
 * - Le prefix de la clef mail à utiliser (ie MAIL_EN_ATTENT_COMPL)
 *
 * @author XDECOOL.EXT
 */
@Setter
@Getter
public class RelanceStatutDemandeConf implements Serializable {

    private static final long serialVersionUID = 6123329536305326942L;
    private String statutARelancer;
    private String cleDelaiAvantPremiereRelance;
    private String cleDelaiEntreDeuxRelances;
    private String clefMailPrefix;

    public RelanceStatutDemandeConf(String statutARelancer, String cleDelaiAvantPremiereRelance,
            String cleDelaiEntreDeuxRelances, String clefMailPrefix) {
        this.statutARelancer = statutARelancer;
        this.cleDelaiAvantPremiereRelance = cleDelaiAvantPremiereRelance;
        this.cleDelaiEntreDeuxRelances = cleDelaiEntreDeuxRelances;
        this.clefMailPrefix = clefMailPrefix;
    }

}
