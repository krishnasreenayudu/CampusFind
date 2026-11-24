package campusfind.dao;

import campusfind.model.Claim;
import campusfind.model.ClaimStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimDAO {
    private static final String SELECT =
            "SELECT c.claim_id, c.item_id, i.title AS item_title, c.claimant_id, u.name AS claimant_name, "
          + "u.roll_no AS claimant_roll, c.status, c.claim_notes, c.admin_remarks, c.created_at, c.decided_at "
          + "FROM claims c JOIN items i ON i.item_id = c.item_id JOIN users u ON u.user_id = c.claimant_id";

    private static Claim map(ResultSet rs) throws SQLException {
        return new Claim(rs.getInt("claim_id"), rs.getInt("item_id"), rs.getString("item_title"),
                rs.getInt("claimant_id"), rs.getString("claimant_name"), rs.getString("claimant_roll"),
                ClaimStatus.valueOf(rs.getString("status")), rs.getString("claim_notes"),
                rs.getString("admin_remarks"), rs.getTimestamp("created_at"), rs.getTimestamp("decided_at"));
    }

    public int insert(Connection c, int itemId, int claimantId, String notes) throws SQLException {
        String sql = "INSERT INTO claims (item_id, claimant_id, claim_notes) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimantId);
            ps.setString(3, notes);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Optional<Claim> findById(Connection c, int claimId) throws SQLException {
        return list(c, SELECT + " WHERE c.claim_id = ?", claimId).stream().findFirst();
    }

    public List<Claim> findByClaimant(Connection c, int userId) throws SQLException {
        return list(c, SELECT + " WHERE c.claimant_id = ? ORDER BY c.created_at DESC, c.claim_id DESC", userId);
    }

    /** All claims, or only those with the given status when status is not null. */
    public List<Claim> findByStatus(Connection c, ClaimStatus status) throws SQLException {
        return status == null
                ? list(c, SELECT + " ORDER BY c.created_at DESC, c.claim_id DESC")
                : list(c, SELECT + " WHERE c.status = ? ORDER BY c.created_at DESC, c.claim_id DESC", status.name());
    }

    public boolean existsForUserAndItem(Connection c, int itemId, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM claims WHERE item_id = ? AND claimant_id = ?")) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateStatus(Connection c, int claimId, ClaimStatus status, String remarks) throws SQLException {
        String sql = "UPDATE claims SET status = ?, admin_remarks = ?, decided_at = NOW() WHERE claim_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, remarks);
            ps.setInt(3, claimId);
            ps.executeUpdate();
        }
    }

    public int countRejected(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM claims WHERE claimant_id = ? AND status = 'REJECTED'")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int countByStatus(Connection c, ClaimStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM claims WHERE status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<Claim> list(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            List<Claim> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        }
    }
}
