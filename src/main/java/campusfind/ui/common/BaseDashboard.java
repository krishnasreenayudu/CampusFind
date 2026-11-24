package campusfind.ui.common;

import campusfind.model.User;
import campusfind.ui.auth.LoginFrame;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Window with a dark sidebar (navigation) and a card area that shows one page at a time. */
public abstract class BaseDashboard extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel nav = new JPanel();
    private final Map<String, JButton> buttons = new HashMap<>();
    private final Map<String, PagePanel> pages = new HashMap<>();

    protected BaseDashboard(String roleLabel) {
        super("CampusFind - " + roleLabel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(22, 20, 18, 20));
        JLabel name = new JLabel("CampusFind");
        name.setFont(Theme.H2);
        name.setForeground(Color.WHITE);
        JLabel role = new JLabel(roleLabel);
        role.setFont(Theme.BODY);
        role.setForeground(new Color(156, 163, 175));
        brand.add(name);
        brand.add(role);

        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        User user = SessionManager.current();
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 20, 20, 20));
        JLabel who = new JLabel(user == null ? "" : user.name());
        who.setFont(Theme.BODY_BOLD);
        who.setForeground(Color.WHITE);
        JButton logout = Theme.secondaryButton("Log out");
        logout.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        footer.add(who);
        footer.add(Box.createVerticalStrut(10));
        footer.add(logout);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        content.setBackground(Theme.BG);
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    protected void addPage(String name, PagePanel page) {
        content.add(page, name);
        pages.put(name, page);
        JButton b = new JButton(name);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(Theme.BODY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        b.addActionListener(e -> show(name));
        nav.add(b);
        nav.add(Box.createVerticalStrut(4));
        buttons.put(name, b);
    }

    protected void show(String name) {
        buttons.forEach((n, b) -> {
            boolean active = n.equals(name);
            b.setContentAreaFilled(active);
            b.setOpaque(active);
            b.setBackground(active ? Theme.PRIMARY : Theme.SIDEBAR);
            b.repaint();
        });
        pages.get(name).refresh();
        cards.show(content, name);
    }
}
