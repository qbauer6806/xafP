package mc.gouv.af.back.util;

import java.util.Map;

import mc.gouv.servicerest.pays.model.PaysBean;

/**
 * Composant permettant de gérer un cache des pays
 * 
 * @author qdeme
 *
 */
public interface PaysCache {

    /**
     * Permet de récupérer la liste "cachée" des pays
     * @return
     */
    public Map<String,PaysBean> getPays();
    
    /**
     * Force le refresh de la liste des pays depuis le WS puis retourne la liste
     * @return
     */
    public Map<String,PaysBean> fetchPays();
    
    /**
     * Permet de retourner la nationalité du pays correspondant à un certain code ISO
     * 
     * @param codePays
     * @return
     */
    public String getNationaliteFromCodeIso(String codePays);
    
    /**
     * Permet de retourner le pays correspondant à un certain code ISO
     * 
     * @param codePays
     * @return
     */
    public PaysBean getPaysFromCodeIso(String codePays);
    
}
