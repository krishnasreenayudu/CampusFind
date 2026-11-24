package campusfind.ui.admin;

import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
import campusfind.model.SearchCriteria;
import campusfind.service.ItemService;
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
import javax.swing.JTextField;

public class ManageItemsPanel extends PagePanel {
    private final ItemService items = new ItemService();
    private final JTextField keyword = Theme.field(14);
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Any", "LOST", "FOUND"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Any", "OPEN", "CLAIM_PENDING", "APPROVED", "RECOVERED"});
    private final StyledTable table = new StyledTable("ID", "Type", "Item", "Category", "Colour", "Location", "Date", "Status", "Reported by");
    private List<Item> shown = List.of();

    public ManageItemsPanel() {
        super("Items", "All lost and found reports");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(new JLabel("Keyword"));
        top.add(keyword);
        top.add(new JLabel("Type"));
        top.add(type);
        top.add(new JLabel("Status"));
        top.add(status);
        JButton search = Theme.primaryButton("Search");
        search.addActionListener(e -> refresh());
        top.add(search);

        JButton recovered = Theme.secondaryButton("Mark lost report recovered");
        JButton delete = Theme.dangerButton("Delete report");
        recovered.addActionListener(e -> markRecovered());
        delete.addActionListener(e -> delete());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(recovered);
        buttons.add(delete);

        body().add(top, BorderLayout.NORTH);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            ItemType t = type.getSelectedIndex() == 0 ? null : ItemType.valueOf((String) type.getSelectedItem());
            ItemStatus s = status.getSelectedIndex() == 0 ? null : ItemStatus.valueOf((String) status.getSelectedItem());
            shown = items.search(new SearchCriteria(keyword.getText(), null, t, null, null, null, null, s));
            List<Object[]> rows = new ArrayList<>();
            for (Item i : shown) {
                rows.add(new Object[]{i.id(), i.type(), i.title(), i.category(), i.color(), i.location(),
                        i.eventDate(), i.status(), i.reporterName()});
            }
            table.setRows(rows);
        });
    }

    private Item selected() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select an item first.");
            return null;
        }
        return shown.get(idx);
    }

    private void markRecovered() {
        Item item = selected();
        if (item != null && Ui.confirm(this, "Mark '" + item.title() + "' as recovered?")) {
            Ui.attempt(this, () -> {
                items.markRecovered(SessionManager.current(), item.id());
                refresh();
            });
        }
    }

    private void delete() {
        Item item = selected();
        if (item != null && Ui.confirm(this, "Permanently delete '" + item.title() + "' and its claims?")) {
            Ui.attempt(this, () -> {
                items.deleteItem(SessionManager.current(), item.id());
                refresh();
            });
        }
    }
}
