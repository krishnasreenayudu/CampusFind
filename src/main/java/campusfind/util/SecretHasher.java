package campusfind.util;

import java.util.Locale;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes the confidential identifying detail of a found item.
 * The text is normalised first (trimmed, lower-cased, spaces collapsed) so that
 * "Blue  Case" and " blue case " match, then hashed with BCrypt. Neither the original
 * secret nor a claimant's answer is ever stored.
 */
public final class SecretHasher {
    private SecretHasher() { }

    public static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public static String hash(String secret) {
        return BCrypt.hashpw(normalize(secret), BCrypt.gensalt(10));
    }

    public static boolean matches(String answer, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(normalize(answer), storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
