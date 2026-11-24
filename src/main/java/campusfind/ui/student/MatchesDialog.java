package campusfind.ui.student;

import campusfind.model.Item;
import campusfind.ui.common.StyledTable;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Suggested matches: found items in the same category with the same colour or a similar location. */
public class MatchesDialog extends JDialog {
    private final StyledTable table = new StyledTable("ID", "Item", "Colour", "Location", "Date", "Found by");
    private final List<Item> matches;

    public MatchesDialog(Window owner, Item lost, List<Item> matches) {
        super(owner, "Suggested matches", ModalityType.APPLICATION_MODAL);
        this.matches = matches;
        setSize(760, 420);
        setLocationRelativeTo(owner);
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        root.add(new JLabel(matches.isEmpty()
                ? "No matching found items yet for '" + lost.title() + "'. Check again later."
                : "Possible matches for '" + lost.title() + "':"), BorderLayout.NORTH);
        List<Object[]> rows = new ArrayList<>();
        for (Item i : matches) {
            rows.add(new Object[]{i.id(), i.title(), i.color(), i.location(), i.eventDate(), i.reporterName()});
        }
        table.setRows(rows);
        root.add(table.scroll(), BorderLayout.CENTER);
        JButton claim = Theme.primaryButton("Claim selected item");
        JButton close = Theme.secondaryButton("Close");
        claim.addActionListener(e -> {
            int idx = table.selectedIndex();
            if (idx < 0) {
                Ui.error(this, "Select an item first.");
                return;
            }
            new ClaimDialog(this, matches.get(idx)).setVisible(true);
        });
        close.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(close);
        buttons.add(claim);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
    }
}
