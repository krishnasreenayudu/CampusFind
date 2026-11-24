package campusfind;

import campusfind.dao.DatabaseSetup;
import campusfind.ui.auth.LoginFrame;
import campusfind.ui.common.Theme;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Theme.install();
        try {
            DatabaseSetup.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "CampusFind could not connect to the database.\n\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and that db.user / db.password in\n"
                    + "src/main/resources/db.properties are correct.",
                    "CampusFind - database error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
