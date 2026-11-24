package campusfind.dao;

import campusfind.model.VerificationAttempt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Log of ownership-verification attempts (the basis of the 3-attempt lock). */
public class AttemptDAO {

    public void insert(Connection c, int itemId, int userId, boolean success) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO verification_attempts (item_id, user_id, is_success) VALUES (?,?,?)")) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            ps.setBoolean(3, success);
            ps.executeUpdate();
        }
    }

    public int countFailed(Connection c, int itemId, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM verification_attempts WHERE item_id = ? AND user_id = ? AND is_success = FALSE")) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<VerificationAttempt> findAll(Connection c) throws SQLException {
        String sql = "SELECT a.attempt_id, a.item_id, i.title, a.user_id, u.name, a.is_success, a.attempted_at "
                + "FROM verification_attempts a JOIN items i ON i.item_id = a.item_id "
                + "JOIN users u ON u.user_id = a.user_id ORDER BY a.attempted_at DESC, a.attempt_id DESC";
        List<VerificationAttempt> list = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new VerificationAttempt(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getString(5), rs.getBoolean(6), rs.getTimestamp(7)));
            }
        }
        return list;
    }
}
