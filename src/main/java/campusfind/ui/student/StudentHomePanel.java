package campusfind.ui.student;

import campusfind.model.ClaimStatus;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.SearchCriteria;
import campusfind.model.User;
import campusfind.service.ClaimService;
import campusfind.service.ItemService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StatCard;
import campusfind.ui.common.Ui;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StudentHomePanel extends PagePanel {
    private final ItemService items = new ItemService();
    private final ClaimService claims = new ClaimService();
    private final StatCard myReports = new StatCard("My reports");
    private final StatCard myClaims = new StatCard("My pending claims");
    private final StatCard openFound = new StatCard("Found items waiting for owners");
    private final StatCard recovered = new StatCard("Items recovered on campus");
    private final JLabel greeting = new JLabel();

    public StudentHomePanel() {
        super("Home", "Your lost and found overview");
        JPanel cards = new JPanel(new GridLayout(2, 2, 16, 16));
        cards.setOpaque(false);
        cards.add(myReports);
        cards.add(myClaims);
        cards.add(openFound);
        cards.add(recovered);
        JPanel tips = campusfind.ui.common.Theme.card();
        tips.setLayout(new BorderLayout());
        tips.add(new JLabel("<html><b>How it works</b><br><br>"
                + "1. Lost something? Use <i>Report Item</i>, then <i>My Reports &gt; Find matches</i>.<br>"
                + "2. Found something? Report it with a confidential detail only the owner would know.<br>"
                + "3. See your item in <i>Search Items</i>? Click <i>Claim selected item</i> and prove ownership "
                + "(3 wrong attempts per item are allowed).<br>"
                + "4. An administrator approves your claim and hands the item over.</html>"));
        greeting.setFont(campusfind.ui.common.Theme.H2);
        body().add(greeting, BorderLayout.NORTH);
        body().add(cards, BorderLayout.CENTER);
        body().add(tips, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        User u = SessionManager.current();
        greeting.setText("Welcome, " + u.name());
        Ui.attempt(this, () -> {
            myReports.setValue(items.myItems(u.id()).size());
            long pending = claims.myClaims(u.id()).stream().filter(c -> c.status() == ClaimStatus.PENDING).count();
            myClaims.setValue(pending);
            openFound.setValue(items.search(new SearchCriteria(null, null, ItemType.FOUND, null, null, null, null,
                    ItemStatus.OPEN)).size());
            recovered.setValue(items.search(new SearchCriteria(null, null, null, null, null, null, null,
                    ItemStatus.RECOVERED)).size());
        });
    }
}
