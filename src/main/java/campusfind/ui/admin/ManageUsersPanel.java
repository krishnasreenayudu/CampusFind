package campusfind.ui.admin;

import campusfind.model.User;
import campusfind.service.AdminService;
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
import javax.swing.JPanel;

public class ManageUsersPanel extends PagePanel {
    private final AdminService admin = new AdminService();
    private final StyledTable table = new StyledTable("ID", "Name", "Roll no.", "Email", "Role", "Flagged", "Registered");
    private List<User> shown = List.of();

    public ManageUsersPanel() {
        super("Users", "Registered accounts (flagged users are shown in red)");
        table.setHighlight(row -> row < shown.size() && shown.get(row).flagged());
        JButton flag = Theme.secondaryButton("Flag / unflag");
        JButton delete = Theme.dangerButton("Delete student");
        flag.addActionListener(e -> toggleFlag());
        delete.addActionListener(e -> delete());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(flag);
        buttons.add(delete);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            shown = admin.users();
            List<Object[]> rows = new ArrayList<>();
            for (User u : shown) {
                rows.add(new Object[]{u.id(), u.name(), u.rollNo(), u.email(), u.role(),
                        u.flagged() ? "YES" : "", Ui.stamp(u.createdAt())});
            }
            table.setRows(rows);
        });
    }

    private User selected() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select a user first.");
            return null;
        }
        return shown.get(idx);
    }

    private void toggleFlag() {
        User u = selected();
        if (u != null) {
            Ui.attempt(this, () -> {
                admin.setFlagged(SessionManager.current(), u.id(), !u.flagged());
                refresh();
            });
        }
    }

    private void delete() {
        User u = selected();
        if (u != null && Ui.confirm(this, "Delete " + u.name() + " and all of their reports and claims?")) {
            Ui.attempt(this, () -> {
                admin.deleteUser(SessionManager.current(), u.id());
                refresh();
            });
        }
    }
}
