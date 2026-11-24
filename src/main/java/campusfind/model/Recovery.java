package campusfind.model;

import java.sql.Timestamp;

public record Recovery(int id, int claimId, String itemTitle, String claimantName,
                       String handedOverBy, String remarks, Timestamp handedOverAt) {
}
