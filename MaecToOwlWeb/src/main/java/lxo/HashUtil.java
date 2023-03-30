package lxo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Tibor Galko, L. Hurtiš
 */
public class HashUtil {

    /**
     * Vytvori md5 hash zo zadaneho retazca
     * @param S
     * @return md5 hash alebo null ak nastane NoSuchAlgorithmException
     */
    public static String getHashableString(String S) {
        S = S.replaceAll("null", "");
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(S));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return null;
    }
}
