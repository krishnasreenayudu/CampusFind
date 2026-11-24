package campusfind.model;

import java.sql.Timestamp;

public record User(int id, String name, String rollNo, String email, Role role,
                   boolean flagged, Timestamp createdAt) {
    public boolean isAdmin() { return role == Role.ADMIN; }
}
