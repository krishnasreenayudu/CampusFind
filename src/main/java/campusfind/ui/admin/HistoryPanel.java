package campusfind.ui.admin;

import campusfind.model.ClaimHistory;
import campusfind.model.Recovery;
import campusfind.model.VerificationAttempt;
import campusfind.service.ClaimService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Ui;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;

/** Complete record: claim events, verification attempts and handovers. */
public class HistoryPanel extends PagePanel {
    private final ClaimService claims = new ClaimService();
    private final StyledTable events = new StyledTable("Claim", "Item", "Claimant", "Event", "From", "To", "By", "Remarks", "When");
    private final StyledTable attempts = new StyledTable("Item", "User", "Result", "When");
    private final StyledTable recoveries = new StyledTable("Claim", "Item", "Received by", "Handed over by", "Remarks", "When");

    public HistoryPanel() {
        super("History", "Claim events, verification attempts and handovers");
        attempts.setHighlight(row -> "FAILED".equals(attempts.getModel().getValueAt(row, 2)));
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Claim events", events.scroll());
        tabs.addTab("Verification attempts", attempts.scroll());
        tabs.addTab("Handovers", recoveries.scroll());
        body().add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            List<Object[]> rows = new ArrayList<>();
            for (ClaimHistory h : claims.history()) {
                rows.add(new Object[]{h.claimId(), h.itemTitle(), h.claimantName(), h.action(),
                        h.oldStatus() == null ? "-" : h.oldStatus(), h.newStatus(),
                        h.changedBy() == null ? "-" : h.changedBy(), h.remarks() == null ? "" : h.remarks(),
                        Ui.stamp(h.changedAt())});
            }
            events.setRows(rows);
            rows = new ArrayList<>();
            for (VerificationAttempt a : claims.attempts()) {
                rows.add(new Object[]{a.itemTitle(), a.userName(), a.success() ? "PASSED" : "FAILED", Ui.stamp(a.attemptedAt())});
            }
            attempts.setRows(rows);
            rows = new ArrayList<>();
            for (Recovery r : claims.recoveries()) {
                rows.add(new Object[]{r.claimId(), r.itemTitle(), r.claimantName(), r.handedOverBy() == null ? "-" : r.handedOverBy(),
                        r.remarks() == null ? "" : r.remarks(), Ui.stamp(r.handedOverAt())});
            }
            recoveries.setRows(rows);
        });
    }
}
