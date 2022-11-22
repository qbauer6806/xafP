package mc.gouv.xaf.back.paiement.data.service;

import java.util.Date;
import java.util.List;

import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;


/**
 * Service permettant la manipulation d'un commande operation
 * @author XDECOOL.EXT
 *
 */
public interface CommandeOperationService {
	
	/**
     * Retournes les commande operation dont l'operation a été acceptée entre la date de départ et d'arrivée
     *
     * @param demarcheId
     * @param startDate
     * @param endDate
     * @return
     */
    List<CommandeOperationDTO> getAllCommandeOperationsAccepteeFilteredByDate(Date startDate, Date endDate);

}
