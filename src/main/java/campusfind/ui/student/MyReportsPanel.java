package campusfind.ui.student;

import campusfind.model.Item;
import campusfind.model.ItemStatus;
import campusfind.model.ItemType;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MyReportsPanel extends PagePanel {
    private final ItemService items = new ItemService();
    private final StyledTable table = new StyledTable("ID", "Type", "Item", "Category", "Colour", "Location", "Date", "Status");
    private List<Item> mine = List.of();

    public MyReportsPanel() {
        super("My Reports", "Items you reported as lost or found");
        JButton matches = Theme.primaryButton("Find matches");
        JButton recovered = Theme.secondaryButton("Mark as recovered");
        JButton delete = Theme.dangerButton("Delete report");
        matches.addActionListener(e -> findMatches());
        recovered.addActionListener(e -> markRecovered());
        delete.addActionListener(e -> delete());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        buttons.add(matches);
        buttons.add(recovered);
        buttons.add(delete);
        body().add(table.scroll(), BorderLayout.CENTER);
        body().add(buttons, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        Ui.attempt(this, () -> {
            mine = items.myItems(SessionManager.current().id());
            List<Object[]> rows = new ArrayList<>();
            for (Item i : mine) {
                rows.add(new Object[]{i.id(), i.type(), i.title(), i.category(), i.color(), i.location(),
                        i.eventDate(), i.status()});
            }
            table.setRows(rows);
        });
    }

    private Item selected() {
        int idx = table.selectedIndex();
        if (idx < 0) {
            Ui.error(this, "Select a report first.");
            return null;
        }
        return mine.get(idx);
    }

    private void findMatches() {
        Item item = selected();
        if (item == null) {
            return;
        }
        if (item.type() != ItemType.LOST) {
            Ui.error(this, "Matches are suggested for LOST reports only.");
            return;
        }
        Ui.attempt(this, () -> new MatchesDialog(SwingUtilities.getWindowAncestor(this), item,
                items.suggestedMatches(item)).setVisible(true));
        refresh();
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
        if (item == null) {
            return;
        }
        if (item.status() != ItemStatus.OPEN) {
            Ui.error(this, "Only reports with status OPEN can be deleted.");
            return;
        }
        if (Ui.confirm(this, "Delete the report '" + item.title() + "'?")) {
            Ui.attempt(this, () -> {
                items.deleteItem(SessionManager.current(), item.id());
                refresh();
            });
        }
    }
}
