package mc.gouv.af.back.pdf;

import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * @author qdeme
 *
 */
public interface PdfService {

    public void generatePdf(DemandeDTO demande) throws Exception;
    
}
