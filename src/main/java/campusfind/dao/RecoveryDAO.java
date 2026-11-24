package campusfind.dao;

import campusfind.model.Recovery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Handover records: one row when an approved item is physically given to its owner. */
public class RecoveryDAO {

    public void insert(Connection c, int claimId, int itemId, int adminId, String remarks) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO recoveries (claim_id, item_id, handed_over_by, remarks) VALUES (?,?,?,?)")) {
            ps.setInt(1, claimId);
            ps.setInt(2, itemId);
            ps.setInt(3, adminId);
            ps.setString(4, remarks);
            ps.executeUpdate();
        }
    }

    public List<Recovery> findAll(Connection c) throws SQLException {
        String sql = "SELECT r.recovery_id, r.claim_id, i.title, cu.name, au.name, r.remarks, r.handed_over_at "
                + "FROM recoveries r JOIN items i ON i.item_id = r.item_id "
                + "JOIN claims c ON c.claim_id = r.claim_id JOIN users cu ON cu.user_id = c.claimant_id "
                + "LEFT JOIN users au ON au.user_id = r.handed_over_by "
                + "ORDER BY r.handed_over_at DESC, r.recovery_id DESC";
        List<Recovery> list = new ArrayList<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Recovery(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getTimestamp(7)));
            }
        }
        return list;
    }
}
