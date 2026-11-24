package campusfind.ui.common;

import campusfind.service.ServiceException;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Dialog and layout helpers used by all screens. */
public final class Ui {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private Ui() { }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "CampusFind", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "CampusFind", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "CampusFind",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Asks for a line of text; returns null if the user cancels. */
    public static String ask(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message, "CampusFind", JOptionPane.QUESTION_MESSAGE);
    }

    /** Runs an action and shows any ServiceException as an error dialog. */
    public static void attempt(Component parent, Runnable action) {
        try {
            action.run();
        } catch (ServiceException e) {
            error(parent, e.getMessage());
        }
    }

    public static String stamp(Timestamp t) {
        return t == null ? "-" : t.toLocalDateTime().format(STAMP);
    }

    /** Adds "label : component" as one row of a GridBagLayout form. */
    public static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(6, 4, 6, 8);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0;
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY_BOLD);
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, g);
    }
}
