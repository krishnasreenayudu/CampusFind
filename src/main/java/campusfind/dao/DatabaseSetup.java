package campusfind.dao;

import campusfind.model.ItemType;
import campusfind.model.Role;
import campusfind.util.DBConnection;
import campusfind.util.PasswordHasher;
import campusfind.util.SecretHasher;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

/** Creates the database and tables on start-up, and seeds the admin account (and optional demo data). */
public final class DatabaseSetup {
    public static final String ADMIN_EMAIL = "admin@campus.edu";
    public static final String ADMIN_PASSWORD = "admin123";

    private DatabaseSetup() { }

    public static void init() throws Exception {
        String db = DBConnection.get("db.name", "campusfind");
        try (Connection c = DBConnection.getServerConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + db
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        try (Connection c = DBConnection.getConnection()) {
            runSchema(c);
            UserDAO users = new UserDAO();
            if (users.countByRole(c, Role.ADMIN) == 0) {
                users.insert(c, "Administrator", "ADMIN001", ADMIN_EMAIL,
                        PasswordHasher.hash(ADMIN_PASSWORD), Role.ADMIN);
            }
            if (Boolean.parseBoolean(DBConnection.get("seed.demo", "false"))
                    && users.countByRole(c, Role.STUDENT) == 0) {
                seedDemo(c, users);
            }
        }
    }

    private static void runSchema(Connection c) throws Exception {
        StringBuilder sql = new StringBuilder();
        try (InputStream in = DatabaseSetup.class.getResourceAsStream("/schema.sql");
             BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.trim().startsWith("--")) {
                    sql.append(line).append('\n');
                }
            }
        }
        try (Statement st = c.createStatement()) {
            for (String statement : sql.toString().split(";")) {
                if (!statement.isBlank()) {
                    st.execute(statement);
                }
            }
        }
    }

    private static void seedDemo(Connection c, UserDAO users) throws Exception {
        String pw = PasswordHasher.hash("student123");
        int rahul = users.insert(c, "Rahul Sharma", "R2024001", "rahul@campus.edu", pw, Role.STUDENT);
        int priya = users.insert(c, "Priya Reddy", "R2024002", "priya@campus.edu", pw, Role.STUDENT);
        users.insert(c, "Arjun Mehta", "R2024003", "arjun@campus.edu", pw, Role.STUDENT);
        ItemDAO items = new ItemDAO();
        LocalDate today = LocalDate.now();
        items.insert(c, priya, ItemType.FOUND, "Black Leather Wallet", "Wallet", "Black", "Library 2nd floor",
                today.minusDays(2), "Found on a study table.", SecretHasher.hash("photo of a golden retriever inside"));
        items.insert(c, priya, ItemType.FOUND, "Wireless Earbuds", "Electronics", "White", "Canteen",
                today.minusDays(3), "Left near the juice counter.", SecretHasher.hash("blue silicone case with a star sticker"));
        items.insert(c, priya, ItemType.FOUND, "Java Programming Textbook", "Books", "Blue", "Lecture Hall 301",
                today.minusDays(1), "Textbook with notes.", SecretHasher.hash("name kiran written on page 10"));
        items.insert(c, rahul, ItemType.LOST, "Black Wallet", "Wallet", "Black", "Library",
                today.minusDays(2), "Lost my wallet while studying.", null);
        items.insert(c, rahul, ItemType.LOST, "Bunch of Keys", "Keys", "Silver", "Sports Complex",
                today.minusDays(4), "Three keys on a red keychain.", null);
    }
}
