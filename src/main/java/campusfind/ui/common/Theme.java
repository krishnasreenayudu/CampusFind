package campusfind.ui.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/** Colours, fonts and small factory methods so every screen looks the same. */
public final class Theme {
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color SIDEBAR = new Color(17, 24, 39);
    public static final Color BG = new Color(243, 244, 246);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(22, 163, 74);

    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font H1 = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font H2 = new Font("Segoe UI", Font.BOLD, 18);

    private Theme() { }

    /** Applies the FlatLaf look and feel when its jar is available, otherwise the system one. */
    public static void install() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // keep the default look and feel
            }
        }
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("defaultFont", BODY);
    }

    private static JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(BODY_BOLD);
        b.setFocusPainted(false);
        b.setMargin(new Insets(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton primaryButton(String text) { return button(text, PRIMARY, Color.WHITE); }

    public static JButton secondaryButton(String text) { return button(text, Color.WHITE, TEXT); }

    public static JButton dangerButton(String text) { return button(text, DANGER, Color.WHITE); }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(H1);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY);
        l.setForeground(MUTED);
        return l;
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(BODY);
        return f;
    }

    /** White rounded-looking panel with a thin border. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return p;
    }
}
