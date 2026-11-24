package campusfind.model;

import java.sql.Timestamp;

public record Claim(int id, int itemId, String itemTitle, int claimantId, String claimantName,
                    String claimantRoll, ClaimStatus status, String notes, String adminRemarks,
                    Timestamp createdAt, Timestamp decidedAt) {
}
