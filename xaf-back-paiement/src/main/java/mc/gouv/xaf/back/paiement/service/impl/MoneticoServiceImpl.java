package mc.gouv.xaf.back.paiement.service.impl;

import com.google.gson.Gson;
import mc.gouv.xaf.back.paiement.service.MoneticoService;
import mc.gouv.xaf.shared.stc.dto.ContexteCommandeDTO;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import mc.gouv.xaf.shared.stc.utils.Base64;
import mc.gouv.xaf.shared.stc.utils.MoneticoPaiementHmac;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoServiceImpl implements MoneticoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoServiceImpl.class);
    private static final String MONETICO_DATE_FORMAT = "dd/MM/yyyy:HH:mm:ss";

    @Override //todo create a dateservice ?
    public String dateFormat(Date date) {
        logStartMethod(LOGGER);
        if (null == date) {
            return "";
        }
        return new SimpleDateFormat(MONETICO_DATE_FORMAT).format(date);
    }

    @Override
    public String contexteCommandeDTOtoBase64(ContexteCommandeDTO contexte) {
        logStartMethod(LOGGER);
        if (null == contexte) {
            return null;
        }
        Gson gson = new Gson();
        String json = gson.toJson(contexte);
        byte[] ptext = json.getBytes(ISO_8859_1);
        String utf8ContexteCommande = new String(ptext, UTF_8);
        return Base64.encode(utf8ContexteCommande.getBytes(UTF_8));
    }

    @Override
    public String getHmacString(PaiementDTO paiementDTO) {
        logStartMethod(LOGGER);
        String sChaineMAC = String.join("*",
                "TPE=" + paiementDTO.getTPE(),
                "ThreeDSecureChallenge=" + paiementDTO.getThreeDSecureChallenge(),
                "contexte_commande=" + paiementDTO.getContexte_commande(),
                "date=" + paiementDTO.getDate(),
                "dateech1=" + paiementDTO.getDateech1(),
                "dateech2=" + paiementDTO.getDateech2(),
                "dateech3=" + paiementDTO.getDateech3(),
                "dateech4=" + paiementDTO.getDateech4(),
                "lgue=" + paiementDTO.getLgue(),
                "mode_affichage=" + paiementDTO.getMode_affichage(),
                "montant=" + paiementDTO.getMontant(),
                "montantech1=" + paiementDTO.getMontantech1(),
                "montantech2=" + paiementDTO.getMontantech2(),
                "montantech3=" + paiementDTO.getMontantech3(),
                "montantech4=" + paiementDTO.getMontantech4(),
                "nbrech=" + paiementDTO.getNbrech(),
                "reference=" + paiementDTO.getReference(),
                "societe=" + paiementDTO.getSociete(),
                "version=" + paiementDTO.getVersion()
        );

        MoneticoPaiementHmac hmac = new MoneticoPaiementHmac();
        try {
            return hmac.computeHmac(sChaineMAC);
        } catch (Exception e) {
            LOGGER.error("Impossible de créer la chaîne MAC", e);
        }
        return null;
    }

}
