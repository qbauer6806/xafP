package mc.gouv.xaf.back.stc.client.monetico;

import mc.gouv.xaf.back.stc.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.stc.service.ReferenceFactoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

import static mc.gouv.xaf.back.stc.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoClient {

    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    private WebTarget target;
    private Client client;
    private String tpe;
    private String companyCode;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    public MoneticoClient(@Qualifier("clientMonetico") Client client) {
        this.client = client;
        this.tpe = MoneticoConfiguration.tpe();
        this.companyCode = MoneticoConfiguration.companyCode();
        this.target = this.client.target(MoneticoConfiguration.serviceUrl());
    }


    public String capture(MoyenPaiementBO paiement) {
        logStartMethod(LOGGER);
        LOGGER.info("Parameters [ MoyenPaiementBO {}] ", paiement);
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateFormat.format(date);

        Response response = this.target
                .queryParam("TPE", this.tpe)
                .queryParam("montant", paiement.getMontantRestant() + "EUR")
                .queryParam("montant_a_capturer", paiement.getMontantRestant() + "EUR")
                .queryParam("montant_deja_capture", "0EUR")
                .queryParam("montant_restant", "0EUR")
                .queryParam("lgue", "FR")
                .queryParam("reference", paiement.getReference())
                .queryParam("date", dateTimeString)
                .queryParam("date_commande", dateTimeString)
                .queryParam("societe", this.companyCode)
                .queryParam("version", MoneticoConfiguration.MONETICOPAIEMENT_VERSION)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String responseString = response.readEntity(String.class);
        LOGGER.info("Return [ responseString {}] ", responseString);
        return responseString;
    }

}
