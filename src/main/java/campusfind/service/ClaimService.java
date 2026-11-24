package campusfind.service;

import campusfind.dao.AttemptDAO;
import campusfind.dao.ClaimDAO;
import campusfind.dao.HistoryDAO;
import campusfind.dao.ItemDAO;
import campusfind.dao.RecoveryDAO;
import campusfind.model.Claim;
import campusfind.model.ClaimHistory;
import campusfind.model.ClaimStatus;
import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.Recovery;
import campusfind.model.User;
import campusfind.model.VerificationAttempt;
import campusfind.util.AppConfig;
import campusfind.util.SecretHasher;
import campusfind.util.Validator;
import java.util.List;

public class ClaimService {
    private final ItemDAO itemDAO = new ItemDAO();
    private final ClaimDAO claimDAO = new ClaimDAO();
    private final AttemptDAO attemptDAO = new AttemptDAO();
    private final HistoryDAO historyDAO = new HistoryDAO();
    private final RecoveryDAO recoveryDAO = new RecoveryDAO();
    private final FraudService fraudService = new FraudService();

    public static final String LOCKED_MESSAGE = "Maximum verification attempts reached for this item.";

    public int attemptsLeft(int userId, int itemId) {
        int failed = Db.read(c -> attemptDAO.countFailed(c, itemId, userId));
        return Math.max(0, AppConfig.MAX_FAILED_ATTEMPTS - failed);
    }

    /**
     * Ownership verification + claim submission, all in one transaction:
     * 1. check the item can be claimed, 2. enforce the 3-failed-attempt limit,
     * 3. compare the (normalised, hashed) answer with the stored hash, 4. log the attempt,
     * 5. on success create the claim, mark the item CLAIM_PENDING and write a history row.
     */
    public ClaimResult submitClaim(User claimant, int itemId, String answer, String notes) {
        if (Validator.isBlank(answer)) {
            throw new ServiceException("Enter the confidential detail of the item.");
        }
        return Db.write(c -> {
            Item item = itemDAO.findByIdForUpdate(c, itemId)
                    .orElseThrow(() -> new ServiceException("Item not found."));
            if (item.type() != ItemType.FOUND) {
                throw new ServiceException("Only found items can be claimed.");
            }
            if (item.status() != ItemStatus.OPEN) {
                throw new ServiceException("This item already has a claim in progress or was recovered.");
            }
            if (item.reporterId() == claimant.id()) {
                throw new ServiceException("You cannot claim an item you reported yourself.");
            }
            if (claimDAO.existsForUserAndItem(c, itemId, claimant.id())) {
                throw new ServiceException("You have already submitted a claim for this item.");
            }
            int failed = attemptDAO.countFailed(c, itemId, claimant.id());
            if (failed >= AppConfig.MAX_FAILED_ATTEMPTS) {
                return new ClaimResult(ClaimResult.Outcome.LOCKED, LOCKED_MESSAGE, 0);
            }
            boolean match = SecretHasher.matches(answer, itemDAO.getSecretHash(c, itemId));
            attemptDAO.insert(c, itemId, claimant.id(), match);
            if (!match) {
                int left = AppConfig.MAX_FAILED_ATTEMPTS - (failed + 1);
                String msg = left == 0
                        ? "Verification failed. " + LOCKED_MESSAGE
                        : "Verification failed. Attempts left for this item: " + left;
                return new ClaimResult(left == 0 ? ClaimResult.Outcome.LOCKED : ClaimResult.Outcome.WRONG_ANSWER,
                        msg, left);
            }
            int claimId = claimDAO.insert(c, itemId, claimant.id(), notes == null ? "" : notes.trim());
            itemDAO.updateStatus(c, itemId, ItemStatus.CLAIM_PENDING);
            historyDAO.insert(c, claimId, null, "PENDING", "SUBMITTED", claimant.id(),
                    "Ownership verification passed");
            return new ClaimResult(ClaimResult.Outcome.VERIFIED,
                    "Ownership verified. Your claim was sent to the administrator for approval.",
                    AppConfig.MAX_FAILED_ATTEMPTS - failed);
        });
    }

    public List<Claim> myClaims(int userId) {
        return Db.read(c -> claimDAO.findByClaimant(c, userId));
    }

    /** Admin view; pass null for all statuses. */
    public List<Claim> claims(ClaimStatus status) {
        return Db.read(c -> claimDAO.findByStatus(c, status));
    }

    public void approve(User admin, int claimId, String remarks) {
        requireAdmin(admin);
        Db.exec(c -> {
            Claim claim = claimDAO.findById(c, claimId).orElseThrow(() -> new ServiceException("Claim not found."));
            if (claim.status() != ClaimStatus.PENDING) {
                throw new ServiceException("Only pending claims can be approved.");
            }
            claimDAO.updateStatus(c, claimId, ClaimStatus.APPROVED, remarks);
            itemDAO.updateStatus(c, claim.itemId(), ItemStatus.APPROVED);
            historyDAO.insert(c, claimId, "PENDING", "APPROVED", "APPROVED", admin.id(), remarks);
        });
    }

    /** Rejects the claim, re-opens the item and runs suspicious-activity detection. Returns true if the claimant got flagged. */
    public boolean reject(User admin, int claimId, String remarks) {
        requireAdmin(admin);
        if (Validator.isBlank(remarks)) {
            throw new ServiceException("Please enter a reason for rejecting the claim.");
        }
        return Db.write(c -> {
            Claim claim = claimDAO.findById(c, claimId).orElseThrow(() -> new ServiceException("Claim not found."));
            if (claim.status() != ClaimStatus.PENDING) {
                throw new ServiceException("Only pending claims can be rejected.");
            }
            claimDAO.updateStatus(c, claimId, ClaimStatus.REJECTED, remarks.trim());
            itemDAO.updateStatus(c, claim.itemId(), ItemStatus.OPEN);
            historyDAO.insert(c, claimId, "PENDING", "REJECTED", "REJECTED", admin.id(), remarks.trim());
            return fraudService.checkAndFlag(c, claim.claimantId());
        });
    }

    /** Records the physical handover: recovery row + item RECOVERED + history row, in one transaction. */
    public void handover(User admin, int claimId, String remarks) {
        requireAdmin(admin);
        Db.exec(c -> {
            Claim claim = claimDAO.findById(c, claimId).orElseThrow(() -> new ServiceException("Claim not found."));
            if (claim.status() != ClaimStatus.APPROVED) {
                throw new ServiceException("Only approved claims can be handed over.");
            }
            Item item = itemDAO.findById(c, claim.itemId()).orElseThrow(() -> new ServiceException("Item not found."));
            if (item.status() == ItemStatus.RECOVERED) {
                throw new ServiceException("This item was already handed over.");
            }
            recoveryDAO.insert(c, claimId, claim.itemId(), admin.id(), remarks);
            itemDAO.updateStatus(c, claim.itemId(), ItemStatus.RECOVERED);
            historyDAO.insert(c, claimId, "APPROVED", "APPROVED", "HANDED_OVER", admin.id(), remarks);
        });
    }

    public List<ClaimHistory> history() { return Db.read(historyDAO::findAll); }

    public List<VerificationAttempt> attempts() { return Db.read(attemptDAO::findAll); }

    public List<Recovery> recoveries() { return Db.read(recoveryDAO::findAll); }

    private static void requireAdmin(User u) {
        if (u == null || !u.isAdmin()) {
            throw new ServiceException("Only administrators can do this.");
        }
    }
}
