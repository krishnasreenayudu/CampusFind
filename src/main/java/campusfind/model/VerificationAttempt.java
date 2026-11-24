package campusfind.model;

import java.sql.Timestamp;

public record VerificationAttempt(int id, int itemId, String itemTitle, int userId,
                                  String userName, boolean success, Timestamp attemptedAt) {
}
