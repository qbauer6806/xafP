package mc.gouv.af.back.pdf;

import mc.gouv.dem.apishared.model.DemandeDTO;

public interface PdfService {

    public void generatePdf(DemandeDTO demande) throws Exception;
    
}
