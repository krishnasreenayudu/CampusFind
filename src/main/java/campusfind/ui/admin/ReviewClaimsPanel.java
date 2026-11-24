package campusfind.ui.admin;

import campusfind.model.Claim;
import campusfind.model.ClaimStatus;
import campusfind.service.ClaimService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ReviewClaimsPanel extends PagePanel {
    private final ClaimService claims = new ClaimService();
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"PENDING", "APPROVED", "REJECTED", "ALL"});
    private final StyledTable table = new StyledTable("Claim", "Item", "Claimant", "Roll no.", "Status", "Extra proof", "Remarks", "Submitted");
    private List<Claim> shown = List.of();

    public ReviewClaimsPanel() {
        super("Claims", "Review claims that passed ownership verification");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Show"));
        top.add(filter);
        filter.addActionListener(e -> refresh());

        JButton approve = Theme.primaryButton("Approve");
        JButton reject = Theme.dangerButton("Reject");
        JButton handover = Theme.secondaryButton("Mark handed over");
        approve.addActionListener(e -> approve());
        reject.addActionListener(e -> reject());
        handover.addActionListener(e -> handover());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(approve);
        buttons.add(reject);
        buttons.add(handover);

        body().add(top, BorderLayout.NORTH);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            String f = (String) filter.getSelectedItem();
            shown = claims.claims("ALL".equals(f) ? null : ClaimStatus.valueOf(f));
            List<Object[]> rows = new ArrayList<>();
            for (Claim c : shown) {
                rows.add(new Object[]{c.id(), c.itemTitle(), c.claimantName(), c.claimantRoll(), c.status(),
                        c.notes() == null ? "" : c.notes(), c.adminRemarks() == null ? "" : c.adminRemarks(),
                        Ui.stamp(c.createdAt())});
            }
            table.setRows(rows);
        });
    }

    private Claim selected() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select a claim first.");
            return null;
        }
        return shown.get(idx);
    }

    private void approve() {
        Claim c = selected();
        if (c != null && Ui.confirm(this, "Approve the claim of " + c.claimantName() + " for '" + c.itemTitle() + "'?")) {
            Ui.attempt(this, () -> {
                claims.approve(SessionManager.current(), c.id(), "Approved after ownership verification");
                refresh();
            });
        }
    }

    private void reject() {
        Claim c = selected();
        if (c == null) {
            return;
        }
        String reason = Ui.ask(this, "Reason for rejecting the claim of " + c.claimantName() + ":");
        if (reason != null) {
            Ui.attempt(this, () -> {
                boolean flagged = claims.reject(SessionManager.current(), c.id(), reason);
                refresh();
                if (flagged) {
                    Ui.info(this, c.claimantName() + " has repeated rejected claims and was flagged as suspicious.");
                }
            });
        }
    }

    private void handover() {
        Claim c = selected();
        if (c != null && Ui.confirm(this, "Confirm that '" + c.itemTitle() + "' was handed over to " + c.claimantName() + "?")) {
            Ui.attempt(this, () -> {
                claims.handover(SessionManager.current(), c.id(), "Item handed over to claimant");
                refresh();
            });
        }
    }
}
