package campusfind.ui.student;

import campusfind.model.Claim;
import campusfind.service.ClaimService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Ui;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class MyClaimsPanel extends PagePanel {
    private final ClaimService claims = new ClaimService();
    private final StyledTable table = new StyledTable("Claim", "Item", "Status", "What happens next", "Admin remarks", "Submitted", "Decided");

    public MyClaimsPanel() {
        super("My Claims", "Track the claims you have submitted");
        body().add(table.scroll(), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            List<Object[]> rows = new ArrayList<>();
            for (Claim c : claims.myClaims(SessionManager.current().id())) {
                String next = switch (c.status()) {
                    case PENDING -> "Waiting for administrator review";
                    case APPROVED -> "Approved - collect the item from the administrator";
                    case REJECTED -> "Claim rejected";
                };
                rows.add(new Object[]{c.id(), c.itemTitle(), c.status(), next,
                        c.adminRemarks() == null ? "" : c.adminRemarks(), Ui.stamp(c.createdAt()), Ui.stamp(c.decidedAt())});
            }
            table.setRows(rows);
        });
    }
}
