package campusfind.service;

import campusfind.dao.ClaimDAO;
import campusfind.dao.ItemDAO;
import campusfind.dao.UserDAO;
import campusfind.model.ClaimStatus;
import campusfind.model.Role;
import campusfind.model.SuspiciousUser;
import campusfind.model.User;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final ClaimDAO claimDAO = new ClaimDAO();

    /** Dashboard numbers (aggregate COUNT queries). */
    public Map<String, Integer> overview() {
        return Db.read(c -> {
            Map<String, Integer> byTypeStatus = itemDAO.countByTypeAndStatus(c);
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("Students", userDAO.countByRole(c, Role.STUDENT));
            m.put("Flagged users", userDAO.countFlagged(c));
            m.put("Open lost reports", byTypeStatus.getOrDefault("LOST_OPEN", 0));
            m.put("Open found items", byTypeStatus.getOrDefault("FOUND_OPEN", 0));
            m.put("Pending claims", claimDAO.countByStatus(c, ClaimStatus.PENDING));
            m.put("Items recovered", byTypeStatus.getOrDefault("LOST_RECOVERED", 0)
                    + byTypeStatus.getOrDefault("FOUND_RECOVERED", 0));
            return m;
        });
    }

    public Map<String, Integer> itemsByCategory() {
        return Db.read(itemDAO::countByCategory);
    }

    public List<User> users() {
        return Db.read(userDAO::findAll);
    }

    public List<SuspiciousUser> suspiciousUsers() {
        return Db.read(userDAO::findSuspicious);
    }

    public void setFlagged(User admin, int userId, boolean flagged) {
        requireAdmin(admin);
        Db.exec(c -> userDAO.setFlagged(c, userId, flagged));
    }

    public void deleteUser(User admin, int userId) {
        requireAdmin(admin);
        Db.exec(c -> {
            User target = userDAO.findById(c, userId).orElseThrow(() -> new ServiceException("User not found."));
            if (target.isAdmin()) {
                throw new ServiceException("Administrator accounts cannot be deleted here.");
            }
            userDAO.delete(c, userId);
        });
    }

    private static void requireAdmin(User u) {
        if (u == null || !u.isAdmin()) {
            throw new ServiceException("Only administrators can do this.");
        }
    }
}
