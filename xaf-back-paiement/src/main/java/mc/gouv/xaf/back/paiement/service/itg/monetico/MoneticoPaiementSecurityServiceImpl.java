package mc.gouv.xaf.back.paiement.service.itg.monetico;

import com.google.gson.Gson;
import mc.gouv.xaf.back.paiement.dto.ContexteCommandeDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.itg.PaiementSecurityService;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Component
public class MoneticoPaiementSecurityServiceImpl implements PaiementSecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneticoPaiementSecurityServiceImpl.class);
    private static final String MONETICO_DATE_FORMAT = "dd/MM/yyyy:HH:mm:ss";
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    @Autowired
    public PaiementPropertiesResolver paiementPropertiesResolver;

    public static String encode(byte[] buf) {
        if (null == buf) {
            return null;
        }

        int size = buf.length;
        char[] ar = new char[((size + 2) / 3) * 4];
        int a = 0;
        int i = 0;
        while (i < size) {
            byte b0 = buf[i++];
            byte b1 = (i < size) ? buf[i++] : 0;
            byte b2 = (i < size) ? buf[i++] : 0;

            int mask = 0x3F;
            ar[a++] = ALPHABET[(b0 >> 2) & mask];
            ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = ALPHABET[b2 & mask];
        }
        int mod = size % 3;
        if (mod == 1 || mod == 2) {
            ar[--a] = '=';
        }

        return new String(ar);
    }

    @Override
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
        return encode(utf8ContexteCommande.getBytes(UTF_8));
    }

    @Override
    public String getHmacStringInterfaceAller(PaiementDTO paiementDTO) {
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
                "mail=" + paiementDTO.getMail(),
                "mode_affichage=" + paiementDTO.getMode_affichage(),
                "montant=" + paiementDTO.getMontant(),
                "montantech1=" + paiementDTO.getMontantech1(),
                "montantech2=" + paiementDTO.getMontantech2(),
                "montantech3=" + paiementDTO.getMontantech3(),
                "montantech4=" + paiementDTO.getMontantech4(),
                "nbrech=" + paiementDTO.getNbrech(),
                "reference=" + paiementDTO.getReference(),
                "societe=" + paiementDTO.getSociete(),
                "texte-libre=" + paiementDTO.getTexteLibre(),
                "url_retour_err=" + paiementDTO.getUrlRetourErr(),
                "url_retour_ok=" + paiementDTO.getUrlRetourOk(),
                "version=" + paiementDTO.getVersion()
        );
        LOGGER.info("CHAINE POUR HMAC : {}", sChaineMAC);

        MoneticoPaiementHmac hmac = new MoneticoPaiementHmac(paiementPropertiesResolver.getPaiementClef());
        try {
            String hmacString = hmac.computeHmac(sChaineMAC);
            LOGGER.info("HMAC : {}", hmacString);
            return hmacString;
        } catch (Exception e) {
            LOGGER.error("Impossible de créer la chaîne MAC pour l'interface ALLER", e);
        }
        return null;
    }

    @Override
    public String getHmacStringInterfaceRetour(MoneticoResponseDTO moneticoResponseDTO) {
        logStartMethod(LOGGER);
        String sChaineMAC = String.join("*",
                "TPE=" + moneticoResponseDTO.getTpe(),
                "authentification=" + moneticoResponseDTO.getAuthentification(),
                "bincb=" + moneticoResponseDTO.getBincb(),
                "brand=" + moneticoResponseDTO.getBrand(),
                "cbmasquee=" + moneticoResponseDTO.getCbmasquee(),
                "code-retour=" + moneticoResponseDTO.getCodeRetour(),
                "cvx=" + moneticoResponseDTO.getCvx(),
                "date=" + moneticoResponseDTO.getDate(),
                "ecard=" + moneticoResponseDTO.getEcard(),
                "hpancb=" + moneticoResponseDTO.getHpancb(),
                "ipclient=" + moneticoResponseDTO.getIpclient(),
                "modepaiement=" + moneticoResponseDTO.getModepaiement(),
                "montant=" + moneticoResponseDTO.getMontant(),
                "motifrefus=" + moneticoResponseDTO.getMotifrefus(),
                "originecb=" + moneticoResponseDTO.getOriginecb(),
                "originetr=" + moneticoResponseDTO.getOriginetr(),
                "reference=" + moneticoResponseDTO.getReference(),
                "texte-libre=" + moneticoResponseDTO.getTexteLibre(),
                "typecompte=" + moneticoResponseDTO.getTypecompte(),
                "usage=" + moneticoResponseDTO.getUsage(),
                "vld=" + moneticoResponseDTO.getVld()
        );
        LOGGER.info("CHAINE POUR HMAC : {}", sChaineMAC);

        MoneticoPaiementHmac hmac = new MoneticoPaiementHmac(paiementPropertiesResolver.getPaiementClef());
        try {
            String hmacString = hmac.computeHmac(sChaineMAC);
            LOGGER.info("HMAC : {}", hmacString);
            return hmacString;
        } catch (Exception e) {
            LOGGER.error("Impossible de créer la chaîne MAC pour l'interface RETOUR", e);
        }
        return null;
    }

}
