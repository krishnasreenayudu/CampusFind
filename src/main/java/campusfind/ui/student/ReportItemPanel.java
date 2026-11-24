package campusfind.ui.student;

import campusfind.model.ItemType;
import campusfind.service.ItemService;
import campusfind.service.ServiceException;
import campusfind.ui.common.PagePanel;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.util.AppConfig;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ReportItemPanel extends PagePanel {
    private final ItemService items = new ItemService();
    private final JRadioButton lost = new JRadioButton("I lost an item", true);
    private final JRadioButton found = new JRadioButton("I found an item");
    private final JTextField title = Theme.field(24);
    private final JComboBox<String> category = new JComboBox<>(AppConfig.CATEGORIES);
    private final JTextField color = Theme.field(24);
    private final JTextField location = Theme.field(24);
    private final JTextField date = Theme.field(24);
    private final JTextArea description = new JTextArea(3, 24);
    private final JTextField secret = Theme.field(24);

    public ReportItemPanel() {
        super("Report an Item", "Tell us what you lost or found");
        ButtonGroup group = new ButtonGroup();
        group.add(lost);
        group.add(found);
        JPanel type = new JPanel(new FlowLayout(FlowLayout.LEFT));
        type.setOpaque(false);
        type.add(lost);
        type.add(found);
        lost.setOpaque(false);
        found.setOpaque(false);

        JPanel form = Theme.card();
        form.setLayout(new java.awt.GridBagLayout());
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(Theme.BODY);
        Ui.addRow(form, 0, "Report type", type);
        Ui.addRow(form, 1, "Item name", title);
        Ui.addRow(form, 2, "Category", category);
        Ui.addRow(form, 3, "Colour", color);
        Ui.addRow(form, 4, "Location", location);
        Ui.addRow(form, 5, "Date (yyyy-MM-dd)", date);
        Ui.addRow(form, 6, "Description", new JScrollPane(description));
        Ui.addRow(form, 7, "Confidential detail", secret);
        JPanel hint = new JPanel(new BorderLayout());
        hint.setOpaque(false);
        hint.add(Theme.muted("Found items only: a detail the real owner would know (e.g. a sticker or what is inside). "
                + "It is stored as a hash, never in readable form."), BorderLayout.CENTER);

        JButton submit = Theme.primaryButton("Submit report");
        submit.addActionListener(e -> submit());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(submit);

        lost.addActionListener(e -> toggleSecret());
        found.addActionListener(e -> toggleSecret());
        toggleSecret();
        date.setText(LocalDate.now().toString());

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(form, BorderLayout.CENTER);
        top.add(hint, BorderLayout.SOUTH);
        body().add(top, BorderLayout.NORTH);
        body().add(south, BorderLayout.CENTER);
    }

    private void toggleSecret() {
        secret.setEnabled(found.isSelected());
        if (!found.isSelected()) {
            secret.setText("");
        }
    }

    private void submit() {
        Ui.attempt(this, () -> {
            LocalDate when;
            try {
                when = LocalDate.parse(date.getText().trim());
            } catch (DateTimeParseException e) {
                throw new ServiceException("Date must be in yyyy-MM-dd format, e.g. 2026-11-14.");
            }
            ItemType type = found.isSelected() ? ItemType.FOUND : ItemType.LOST;
            items.reportItem(SessionManager.current(), type, title.getText(), (String) category.getSelectedItem(),
                    color.getText(), location.getText(), when, description.getText(), secret.getText());
            Ui.info(this, "Your report was saved.");
            title.setText("");
            color.setText("");
            location.setText("");
            description.setText("");
            secret.setText("");
            date.setText(LocalDate.now().toString());
        });
    }
}
