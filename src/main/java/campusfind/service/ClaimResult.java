package campusfind.service;

/** Outcome of an ownership-verification attempt. */
public record ClaimResult(Outcome outcome, String message, int attemptsLeft) {
    public enum Outcome { VERIFIED, WRONG_ANSWER, LOCKED }
}
