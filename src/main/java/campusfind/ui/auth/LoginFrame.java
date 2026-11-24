package campusfind.ui.auth;

import campusfind.model.Role;
import campusfind.model.User;
import campusfind.service.AuthService;
import campusfind.ui.admin.AdminDashboard;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.ui.student.StudentDashboard;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Administrator"});
    private final JTextField identifier = Theme.field(20);
    private final JPasswordField password = new JPasswordField(20);

    public LoginFrame() {
        super("CampusFind - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(880, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel left = new JPanel();
        left.setBackground(Theme.SIDEBAR);
        left.setPreferredSize(new Dimension(340, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(150, 32, 32, 32));
        JLabel brand = new JLabel("CampusFind");
        brand.setFont(Theme.H1);
        brand.setForeground(Color.WHITE);
        JLabel tag = new JLabel("<html>Smart Campus Lost &amp; Found.<br>Report, search, claim and recover.</html>");
        tag.setFont(Theme.BODY);
        tag.setForeground(new Color(156, 163, 175));
        left.add(brand);
        left.add(Box.createVerticalStrut(10));
        left.add(tag);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Ui.addRow(form, 0, "Login as", roleBox);
        Ui.addRow(form, 1, "Email or roll no.", identifier);
        Ui.addRow(form, 2, "Password", password);

        JButton login = Theme.primaryButton("Log in");
        login.addActionListener(e -> doLogin());
        JButton register = Theme.secondaryButton("Create a student account");
        register.addActionListener(e -> new RegisterFrame(this).setVisible(true));
        getRootPane().setDefaultButton(login);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 4, 0);
        right.add(Theme.title("Welcome back"), g);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 18, 0);
        right.add(Theme.muted("Log in to continue"), g);
        g.gridy = 2;
        g.insets = new Insets(0, 0, 12, 0);
        right.add(form, g);
        g.gridy = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        right.add(login, g);
        g.gridy = 4;
        g.insets = new Insets(0, 0, 0, 0);
        right.add(register, g);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private void doLogin() {
        Ui.attempt(this, () -> {
            Role role = roleBox.getSelectedIndex() == 0 ? Role.STUDENT : Role.ADMIN;
            User user = auth.login(identifier.getText(), new String(password.getPassword()), role);
            SessionManager.login(user);
            dispose();
            if (user.isAdmin()) {
                new AdminDashboard().setVisible(true);
            } else {
                new StudentDashboard().setVisible(true);
            }
        });
    }
}
