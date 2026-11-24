package campusfind.model;

import java.time.LocalDate;

/** Filters for searching items. Null or blank fields are ignored. */
public record SearchCriteria(String keyword, String category, ItemType type, String color,
                             String location, LocalDate from, LocalDate to, ItemStatus status) {
    public static SearchCriteria all() {
        return new SearchCriteria(null, null, null, null, null, null, null, null);
    }
}
