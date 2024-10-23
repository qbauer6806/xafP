package mc.gouv.xaf.back.paiement.service.itg.monetico;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*****************************************************************************
 *
 * "Open source" kit for Monetico Paiement (TM)
 *
 * Author   : Euro-Information/e-Commerce
 * Version  : 4.0
 * Date      : 06/06/2014
 *
 * Copyright: (c) 2014 Euro-Information. All rights reserved.
 *
 *****************************************************************************/
public final class MoneticoPaiementHmac {

    private final String sUsableKey;

    public MoneticoPaiementHmac(String moneticoPaiementKey) {
        this.sUsableKey = getUsableKey(moneticoPaiementKey);
    }

    /**
     * convert hex String to Byte Array
     */
    private static int charToNibble(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 0xa;
        } else if ('A' <= c && c <= 'F') {
            return c - 'A' + 0xa;
        } else {
            throw new IllegalArgumentException("Invalid hex characters");
        }
    }

    private static byte[] hexStringToByteArray(String hs) {
        if (hs == null) {
            return new byte[] {};
        }
        int hslength = hs.length();
        if ((hslength & 0 * 1) != 0) {
            throw new IllegalArgumentException(" hexStringToByteArray" + " requires an even number of hex characters");
        }
        int hsstart = 0;
        if (hs.startsWith("0x")) {
            hsstart += 2;
        }
        byte[] ba = new byte[(hslength - hsstart) / 2];
        for (int i = hsstart, j = 0; i < hslength; i += 2, j++) {
            int high = charToNibble(hs.charAt(i));
            int low = charToNibble(hs.charAt(i + 1));
            ba[j] = (byte) ((high << 4) | low);
        }
        return ba;
    }

    /**
     * convert byte Array to Hex String
     */
    private static String byteArrayToHexString(byte[] ba) {
        char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
                'f' };  // to convert a 4bit-byte to a hex char.
        StringBuilder sb = new StringBuilder(ba.length * 2);
        for (byte b : ba) {
            sb.append(hexChar[(b & 0xf0) >>> 4]);
            // look up high nibble half-byte
            sb.append(hexChar[b & 0x0f]);
            // look up low nibble half-byte
        }
        return sb.toString();
    }

    private String getUsableKey(String moneticoPaiementKey) {
        String hexStrKey = moneticoPaiementKey.substring(0, 38);
        String hexFinal = "" + moneticoPaiementKey.substring(38, 40) + "00";
        int cca0 = hexFinal.charAt(0);
        if (cca0 > 70 && cca0 < 97) {
            hexStrKey += (char) (cca0 - 23) + hexFinal.substring(1, 2);
        } else {
            if (hexFinal.charAt(1) == 'M') {
                hexStrKey += hexFinal.charAt(0) + "0";
            } else {
                hexStrKey += hexFinal.substring(0, 2);
            }
        }
        return hexStrKey;
    }

    /**
     * compute Hmac for the given string
     */
    public String computeHmac(String sData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac oHmac = Mac.getInstance("HmacSHA1");
        SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(this.sUsableKey), oHmac.getAlgorithm());
        oHmac.init(sks);
        oHmac.reset();
        byte[] resMAC = oHmac.doFinal(sData.getBytes());
        oHmac.reset();
        return byteArrayToHexString(resMAC);
    }

    /**
     * check validity of Hmac
     */
    public Boolean isValidHmac(String sChaineMac, String sSentMac) {
        return sChaineMac.compareToIgnoreCase(sSentMac) == 0;
    }

}
