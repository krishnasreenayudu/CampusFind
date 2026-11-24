package campusfind.model;

public record SuspiciousUser(User user, int rejectedClaims, int failedAttempts) {
}
