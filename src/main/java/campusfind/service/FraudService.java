package campusfind.service;

import campusfind.dao.ClaimDAO;
import campusfind.dao.UserDAO;
import campusfind.util.AppConfig;
import java.sql.Connection;
import java.sql.SQLException;

/** Suspicious-activity detection: repeated rejected claims get the user flagged for admin review. */
public class FraudService {
    private final ClaimDAO claimDAO = new ClaimDAO();
    private final UserDAO userDAO = new UserDAO();

    /** Call inside the transaction that rejects a claim. Returns true if the user was just flagged. */
    public boolean checkAndFlag(Connection c, int userId) throws SQLException {
        int rejected = claimDAO.countRejected(c, userId);
        boolean alreadyFlagged = userDAO.findById(c, userId).map(u -> u.flagged()).orElse(true);
        if (rejected >= AppConfig.FLAG_THRESHOLD && !alreadyFlagged) {
            userDAO.setFlagged(c, userId, true);
            return true;
        }
        return false;
    }
}
