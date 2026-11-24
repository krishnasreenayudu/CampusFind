package campusfind.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/** A lost or found report. The secret (confidential detail) is never part of this object. */
public record Item(int id, int reporterId, String reporterName, ItemType type, String title,
                   String category, String color, String location, LocalDate eventDate,
                   String description, ItemStatus status, Timestamp createdAt) {
}
