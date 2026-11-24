package campusfind.util;

import org.mindrot.jbcrypt.BCrypt;

/** BCrypt password hashing. BCrypt generates its own salt and stores it inside the hash. */
public final class PasswordHasher {
    private PasswordHasher() { }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean verify(String password, String storedHash) {
        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
