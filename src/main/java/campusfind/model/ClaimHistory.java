package campusfind.model;

import java.sql.Timestamp;

public record ClaimHistory(int id, int claimId, String itemTitle, String claimantName,
                           String oldStatus, String newStatus, String action, String changedBy,
                           String remarks, Timestamp changedAt) {
}
