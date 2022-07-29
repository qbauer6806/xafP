package mc.gouv.xaf.back.paiement.controller;

import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.dao.OperationRepository;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.shared.stc.MoyenPaiementDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractPaiementApiController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @GetMapping
    public PaiementDTO getPaiement(@RequestParam String demandesId,
                                   @RequestParam String langue,
                                   @RequestParam Integer usagerId,
                                   @RequestParam boolean iframe) {

        return paiementService.create(demandesId, langue, usagerId, iframe);
    }

    @PostMapping
    public void updatePaiement(@RequestBody MoyenPaiementDTO moyenPaiementDTO) {
        paiementService.updateStatus(moyenPaiementDTO);
    }

    @GetMapping("stats/operation")
    public void getStatsOperation(HttpServletResponse response) {
        try {
            response.getWriter().println(OperationBO.headerCSV());
            response.setContentType("text/plain; charset=utf-8");
            operationRepository.findAll().forEach(operationBO -> {
                try {
                    response.getWriter().println(operationBO.toCSV());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            response.getWriter().close();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }

    }

    @GetMapping("stats/moyen-paiement")
    public void getStatsMoyenPaiement(HttpServletResponse response) {
        try {
            response.getWriter().println(MoyenPaiementBO.headerCSV());
            response.setContentType("text/plain; charset=utf-8");
            moyenPaiementRepository.findAll().forEach(moyenPaiementBO -> {
                try {
                    response.getWriter().println(moyenPaiementBO.toCSV());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            response.getWriter().close();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }

    }

}
