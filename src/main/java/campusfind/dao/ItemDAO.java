package campusfind.dao;

import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.SearchCriteria;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemDAO {
    private static final String SELECT =
            "SELECT i.item_id, i.reporter_id, u.name AS reporter_name, i.type, i.title, i.category, "
          + "i.color, i.location, i.event_date, i.description, i.status, i.created_at "
          + "FROM items i JOIN users u ON u.user_id = i.reporter_id";

    private static Item map(ResultSet rs) throws SQLException {
        return new Item(rs.getInt("item_id"), rs.getInt("reporter_id"), rs.getString("reporter_name"),
                ItemType.valueOf(rs.getString("type")), rs.getString("title"), rs.getString("category"),
                rs.getString("color"), rs.getString("location"), rs.getDate("event_date").toLocalDate(),
                rs.getString("description"), ItemStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at"));
    }

    public int insert(Connection c, int reporterId, ItemType type, String title, String category,
                      String color, String location, java.time.LocalDate date, String description,
                      String secretHash) throws SQLException {
        String sql = "INSERT INTO items (reporter_id, type, title, category, color, location, event_date, "
                + "description, secret_hash) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reporterId);
            ps.setString(2, type.name());
            ps.setString(3, title);
            ps.setString(4, category);
            ps.setString(5, color);
            ps.setString(6, location);
            ps.setDate(7, Date.valueOf(date));
            ps.setString(8, description);
            ps.setString(9, secretHash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** Dynamic search: only the filters that are filled in are added to the WHERE clause. */
    public List<Item> search(Connection c, SearchCriteria f) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (notBlank(f.keyword())) {
            sql.append(" AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ?)");
            String like = "%" + f.keyword().trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (notBlank(f.category())) {
            sql.append(" AND i.category = ?");
            params.add(f.category());
        }
        if (f.type() != null) {
            sql.append(" AND i.type = ?");
            params.add(f.type().name());
        }
        if (notBlank(f.color())) {
            sql.append(" AND LOWER(i.color) LIKE ?");
            params.add("%" + f.color().trim().toLowerCase() + "%");
        }
        if (notBlank(f.location())) {
            sql.append(" AND LOWER(i.location) LIKE ?");
            params.add("%" + f.location().trim().toLowerCase() + "%");
        }
        if (f.from() != null) {
            sql.append(" AND i.event_date >= ?");
            params.add(Date.valueOf(f.from()));
        }
        if (f.to() != null) {
            sql.append(" AND i.event_date <= ?");
            params.add(Date.valueOf(f.to()));
        }
        if (f.status() != null) {
            sql.append(" AND i.status = ?");
            params.add(f.status().name());
        }
        sql.append(" ORDER BY i.created_at DESC, i.item_id DESC");
        return query(c, sql.toString(), params);
    }

    public List<Item> findByReporter(Connection c, int userId) throws SQLException {
        return query(c, SELECT + " WHERE i.reporter_id = ? ORDER BY i.created_at DESC, i.item_id DESC",
                List.of(userId));
    }

    /** Open FOUND items in the same category that share the colour or the location of a lost item. */
    public List<Item> findMatchesForLost(Connection c, Item lost) throws SQLException {
        String sql = SELECT + " WHERE i.type = 'FOUND' AND i.status = 'OPEN' AND i.category = ? "
                + "AND (LOWER(i.color) = ? OR LOWER(i.location) LIKE ?) AND i.reporter_id <> ? "
                + "ORDER BY i.event_date DESC";
        return query(c, sql, List.of(lost.category(), lost.color().toLowerCase(),
                "%" + lost.location().toLowerCase() + "%", lost.reporterId()));
    }

    public Optional<Item> findById(Connection c, int id) throws SQLException {
        return query(c, SELECT + " WHERE i.item_id = ?", List.of(id)).stream().findFirst();
    }

    /** Same as findById but locks the row until the transaction ends (prevents two simultaneous claims). */
    public Optional<Item> findByIdForUpdate(Connection c, int id) throws SQLException {
        return query(c, SELECT + " WHERE i.item_id = ? FOR UPDATE", List.of(id)).stream().findFirst();
    }

    public String getSecretHash(Connection c, int itemId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT secret_hash FROM items WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void updateStatus(Connection c, int itemId, ItemStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE items SET status = ? WHERE item_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, int itemId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM items WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }
    }

    /** Aggregate: number of items per type and status, keyed like "FOUND_OPEN". */
    public Map<String, Integer> countByTypeAndStatus(Connection c) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT type, status, COUNT(*) AS n FROM items GROUP BY type, status")) {
            while (rs.next()) {
                map.put(rs.getString("type") + "_" + rs.getString("status"), rs.getInt("n"));
            }
        }
        return map;
    }

    /** Aggregate: number of items per category, most common first. */
    public Map<String, Integer> countByCategory(Connection c) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT category, COUNT(*) AS n FROM items GROUP BY category ORDER BY n DESC, category")) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getInt("n"));
            }
        }
        return map;
    }

    private List<Item> query(Connection c, String sql, List<?> params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Date d) {
                    ps.setDate(i + 1, d);
                } else if (p instanceof Integer n) {
                    ps.setInt(i + 1, n);
                } else {
                    ps.setString(i + 1, String.valueOf(p));
                }
            }
            List<Item> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        }
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
}
