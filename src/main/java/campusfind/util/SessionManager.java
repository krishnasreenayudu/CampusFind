package campusfind.util;

import campusfind.model.User;

/** Holds the user who is currently logged in. */
public final class SessionManager {
    private static User current;

    private SessionManager() { }

    public static void login(User user) { current = user; }

    public static void logout() { current = null; }

    public static User current() { return current; }
}
