package mc.gouv.xaf.shared.stc.utils;

/**
 * Encodeur en Base 64.
 * Source: https://stackoverflow.com/a/4265472/1307778
 *
 * @author mboutelier.ext
 */
public class Base64 {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static final int[] toInt = new int[128];

    static {
        for (int i = 0; i < ALPHABET.length; i++) {
            toInt[ALPHABET[i]] = i;
        }
    }

    /**
     * Traduit un tableau de byte en une String en Base64.
     *
     * @param buf le tableau de byte
     * @return la String traduite en Base64 ou null si buf est null
     */
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
}
