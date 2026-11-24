package campusfind.util;

/** Application-wide constants. Change them here to tune the rules. */
public final class AppConfig {
    /** Failed ownership-verification attempts allowed per user per item. */
    public static final int MAX_FAILED_ATTEMPTS = 3;
    /** A user with this many rejected claims is flagged as suspicious. */
    public static final int FLAG_THRESHOLD = 3;

    public static final String[] CATEGORIES = {
        "Electronics", "Books", "ID Card", "Wallet", "Bag", "Keys", "Clothing", "Accessories", "Other"
    };

    private AppConfig() { }
}
