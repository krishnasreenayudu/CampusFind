package campusfind.ui.student;

import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.SearchCriteria;
import campusfind.service.ItemService;
import campusfind.service.ServiceException;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.util.AppConfig;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SearchItemsPanel extends PagePanel {
    private final ItemService items = new ItemService();
    private final JTextField keyword = Theme.field(14);
    private final JComboBox<String> category = new JComboBox<>();
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Any", "LOST", "FOUND"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{"OPEN", "Any", "CLAIM_PENDING", "APPROVED", "RECOVERED"});
    private final JTextField color = Theme.field(8);
    private final JTextField location = Theme.field(10);
    private final JTextField from = Theme.field(8);
    private final JTextField to = Theme.field(8);
    private final StyledTable table = new StyledTable("ID", "Type", "Item", "Category", "Colour", "Location", "Date", "Status", "Reported by");
    private final JTextArea details = new JTextArea(3, 40);
    private List<Item> results = List.of();

    public SearchItemsPanel() {
        super("Search Items", "Find lost and found items by category, colour, location or date");
        category.addItem("All");
        for (String c : AppConfig.CATEGORIES) {
            category.addItem(c);
        }
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Keyword"));
        row1.add(keyword);
        row1.add(new JLabel("Category"));
        row1.add(category);
        row1.add(new JLabel("Type"));
        row1.add(type);
        row1.add(new JLabel("Status"));
        row1.add(status);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Colour"));
        row2.add(color);
        row2.add(new JLabel("Location"));
        row2.add(location);
        row2.add(new JLabel("From (yyyy-MM-dd)"));
        row2.add(from);
        row2.add(new JLabel("To"));
        row2.add(to);
        JButton search = Theme.primaryButton("Search");
        JButton clear = Theme.secondaryButton("Clear");
        search.addActionListener(e -> refresh());
        clear.addActionListener(e -> clearFilters());
        row2.add(search);
        row2.add(clear);
        JPanel filters = Theme.card();
        filters.setLayout(new BoxLayout(filters, BoxLayout.Y_AXIS));
        filters.add(row1);
        filters.add(row2);

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(Theme.BODY);
        table.getSelectionModel().addListSelectionListener(e -> showDetails());

        JButton claim = Theme.primaryButton("Claim selected item");
        claim.addActionListener(e -> claimSelected());
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(details, BorderLayout.CENTER);
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btn.setOpaque(false);
        btn.add(claim);
        south.add(btn, BorderLayout.SOUTH);

        body().add(filters, BorderLayout.NORTH);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(south, BorderLayout.SOUTH);
    }

    private LocalDate parse(JTextField f) {
        if (f.getText().isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(f.getText().trim());
        } catch (DateTimeParseException e) {
            throw new ServiceException("Dates must be in yyyy-MM-dd format.");
        }
    }

    private void clearFilters() {
        keyword.setText("");
        color.setText("");
        location.setText("");
        from.setText("");
        to.setText("");
        category.setSelectedIndex(0);
        type.setSelectedIndex(0);
        status.setSelectedIndex(0);
        refresh();
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            String cat = category.getSelectedIndex() <= 0 ? null : (String) category.getSelectedItem();
            ItemType t = type.getSelectedIndex() == 0 ? null : ItemType.valueOf((String) type.getSelectedItem());
            ItemStatus s = status.getSelectedIndex() == 1 ? null : ItemStatus.valueOf((String) status.getSelectedItem());
            results = items.search(new SearchCriteria(keyword.getText(), cat, t, color.getText(),
                    location.getText(), parse(from), parse(to), s));
            List<Object[]> rows = new ArrayList<>();
            for (Item i : results) {
                rows.add(new Object[]{i.id(), i.type(), i.title(), i.category(), i.color(), i.location(),
                        i.eventDate(), i.status(), i.reporterName()});
            }
            table.setRows(rows);
            details.setText("");
        });
    }

    private void showDetails() {
        int idx = table.selectedIndex();
        details.setText(idx < 0 || idx >= results.size() ? "" : "Description: " + results.get(idx).description());
    }

    private void claimSelected() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select an item first.");
            return;
        }
        Item item = results.get(idx);
        if (item.type() != ItemType.FOUND) {
            Ui.error(this, "Only FOUND items can be claimed. For a lost item, see My Reports > Find matches.");
            return;
        }
        if (item.status() != ItemStatus.OPEN) {
            Ui.error(this, "This item already has a claim in progress or was recovered.");
            return;
        }
        if (item.reporterId() == SessionManager.current().id()) {
            Ui.error(this, "You reported this item yourself.");
            return;
        }
        new ClaimDialog(SwingUtilities.getWindowAncestor(this), item).setVisible(true);
        refresh();
    }
}
