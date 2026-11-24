package campusfind.ui.admin;

import campusfind.model.SuspiciousUser;
import campusfind.service.AdminService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.util.AppConfig;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

public class SuspiciousUsersPanel extends PagePanel {
    private final AdminService admin = new AdminService();
    private final StyledTable table = new StyledTable("ID", "Name", "Roll no.", "Email", "Rejected claims", "Failed verifications");
    private List<SuspiciousUser> shown = List.of();

    public SuspiciousUsersPanel() {
        super("Suspicious Users", "Users with " + AppConfig.FLAG_THRESHOLD + " or more rejected claims are flagged automatically");
        table.setHighlight(row -> true);
        JButton clear = Theme.primaryButton("Clear flag (reviewed)");
        clear.addActionListener(e -> clearFlag());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(clear);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            shown = admin.suspiciousUsers();
            List<Object[]> rows = new ArrayList<>();
            for (SuspiciousUser s : shown) {
                rows.add(new Object[]{s.user().id(), s.user().name(), s.user().rollNo(), s.user().email(),
                        s.rejectedClaims(), s.failedAttempts()});
            }
            table.setRows(rows);
        });
    }

    private void clearFlag() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select a user first.");
            return;
        }
        SuspiciousUser s = shown.get(idx);
        if (Ui.confirm(this, "Clear the flag on " + s.user().name() + "?")) {
            Ui.attempt(this, () -> {
                admin.setFlagged(SessionManager.current(), s.user().id(), false);
                refresh();
            });
        }
    }
}
