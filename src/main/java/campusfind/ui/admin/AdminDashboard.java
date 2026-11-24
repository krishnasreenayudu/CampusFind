package campusfind.ui.admin;

import campusfind.ui.common.BaseDashboard;

public class AdminDashboard extends BaseDashboard {
    public AdminDashboard() {
        super("Administrator");
        addPage("Overview", new AdminOverviewPanel());
        addPage("Claims", new ReviewClaimsPanel());
        addPage("Items", new ManageItemsPanel());
        addPage("Users", new ManageUsersPanel());
        addPage("Suspicious Users", new SuspiciousUsersPanel());
        addPage("History", new HistoryPanel());
        show("Overview");
    }
}
