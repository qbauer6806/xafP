package mc.gouv.af.back.service;

/**
 * 
 * Service implémenté par la démarche permettant de fournir à af-back des informations propres
 * à chaque démarche.
 * 
 * @author qdeme
 *
 */
public interface DemarchesDataProvider {
    
    public String getStatusLibelle(String status);

}
