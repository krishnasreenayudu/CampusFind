package campusfind.util;

import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern ROLL = Pattern.compile("^[A-Za-z0-9_-]{3,30}$");

    private Validator() { }

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static boolean validEmail(String s) { return s != null && EMAIL.matcher(s.trim()).matches(); }

    public static boolean validRoll(String s) { return s != null && ROLL.matcher(s.trim()).matches(); }

    public static boolean validPassword(String s) { return s != null && s.length() >= 6; }
}
