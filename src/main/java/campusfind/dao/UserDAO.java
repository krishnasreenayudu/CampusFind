package campusfind.dao;

import campusfind.model.Role;
import campusfind.model.SuspiciousUser;
import campusfind.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final String COLS = "user_id, name, roll_no, email, role, is_flagged, created_at";

    /** A user together with the stored password hash (used only for login). */
    public record Credentials(User user, String passwordHash) { }

    static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("name"), rs.getString("roll_no"),
                rs.getString("email"), Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_flagged"), rs.getTimestamp("created_at"));
    }

    public int insert(Connection c, String name, String roll, String email, String hash, Role role)
            throws SQLException {
        String sql = "INSERT INTO users (name, roll_no, email, password_hash, role) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, roll);
            ps.setString(3, email);
            ps.setString(4, hash);
            ps.setString(5, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean emailExists(Connection c, String email) throws SQLException {
        return exists(c, "SELECT 1 FROM users WHERE LOWER(email) = ?", email.toLowerCase());
    }

    public boolean rollExists(Connection c, String roll) throws SQLException {
        return exists(c, "SELECT 1 FROM users WHERE UPPER(roll_no) = ?", roll.toUpperCase());
    }

    private boolean exists(Connection c, String sql, String value) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Finds a user by email or roll number, with the password hash. */
    public Optional<Credentials> findForLogin(Connection c, String identifier) throws SQLException {
        String sql = "SELECT " + COLS + ", password_hash FROM users "
                + "WHERE LOWER(email) = ? OR UPPER(roll_no) = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier.toLowerCase());
            ps.setString(2, identifier.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Credentials(map(rs), rs.getString("password_hash")))
                        : Optional.empty();
            }
        }
    }

    public Optional<User> findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM users WHERE user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAll(Connection c) throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLS + " FROM users ORDER BY role, name")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void setFlagged(Connection c, int userId, boolean flagged) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET is_flagged = ? WHERE user_id = ?")) {
            ps.setBoolean(1, flagged);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public int countByRole(Connection c, Role role) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countFlagged(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE is_flagged = TRUE")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Flagged users with their rejected-claim and failed-verification counts (correlated subqueries). */
    public List<SuspiciousUser> findSuspicious(Connection c) throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.roll_no, u.email, u.role, u.is_flagged, u.created_at, "
                + "(SELECT COUNT(*) FROM claims cl WHERE cl.claimant_id = u.user_id AND cl.status = 'REJECTED') AS rejected, "
                + "(SELECT COUNT(*) FROM verification_attempts a WHERE a.user_id = u.user_id AND a.is_success = FALSE) AS failed "
                + "FROM users u WHERE u.is_flagged = TRUE ORDER BY rejected DESC, failed DESC";
        List<SuspiciousUser> list = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SuspiciousUser(map(rs), rs.getInt("rejected"), rs.getInt("failed")));
            }
        }
        return list;
    }
}
