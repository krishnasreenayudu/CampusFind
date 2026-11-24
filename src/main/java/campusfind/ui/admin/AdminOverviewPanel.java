package campusfind.ui.admin;

import campusfind.service.AdminService;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StatCard;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AdminOverviewPanel extends PagePanel {
    private final AdminService admin = new AdminService();
    private final Map<String, StatCard> cards = new LinkedHashMap<>();
    private final StyledTable categories = new StyledTable("Category", "Reports");

    public AdminOverviewPanel() {
        super("Overview", "Campus lost and found at a glance");
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        for (String name : new String[]{"Students", "Flagged users", "Open lost reports",
                "Open found items", "Pending claims", "Items recovered"}) {
            StatCard card = new StatCard(name);
            cards.put(name, card);
            grid.add(card);
        }
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        JLabel label = new JLabel("Reports by category");
        label.setFont(Theme.H2);
        south.add(label, BorderLayout.NORTH);
        south.add(categories.scroll(), BorderLayout.CENTER);
        south.setPreferredSize(new Dimension(0, 230));
        body().add(grid, BorderLayout.CENTER);
        body().add(south, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            admin.overview().forEach((k, v) -> cards.get(k).setValue(v));
            List<Object[]> rows = new ArrayList<>();
            admin.itemsByCategory().forEach((k, v) -> rows.add(new Object[]{k, v}));
            categories.setRows(rows);
        });
    }
}
