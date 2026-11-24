package campusfind.dao;

import campusfind.model.ClaimHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Audit trail of every claim event: submitted, approved, rejected, handed over. */
public class HistoryDAO {

    public void insert(Connection c, int claimId, String oldStatus, String newStatus, String action,
                       int changedBy, String remarks) throws SQLException {
        String sql = "INSERT INTO claim_history (claim_id, old_status, new_status, action_type, changed_by, remarks) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            if (oldStatus == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, oldStatus);
            }
            ps.setString(3, newStatus);
            ps.setString(4, action);
            ps.setInt(5, changedBy);
            ps.setString(6, remarks);
            ps.executeUpdate();
        }
    }

    public List<ClaimHistory> findAll(Connection c) throws SQLException {
        String sql = "SELECT h.history_id, h.claim_id, i.title, cu.name, h.old_status, h.new_status, "
                + "h.action_type, au.name, h.remarks, h.changed_at "
                + "FROM claim_history h JOIN claims c ON c.claim_id = h.claim_id "
                + "JOIN items i ON i.item_id = c.item_id JOIN users cu ON cu.user_id = c.claimant_id "
                + "LEFT JOIN users au ON au.user_id = h.changed_by "
                + "ORDER BY h.changed_at DESC, h.history_id DESC";
        List<ClaimHistory> list = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ClaimHistory(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getTimestamp(10)));
            }
        }
        return list;
    }
}
