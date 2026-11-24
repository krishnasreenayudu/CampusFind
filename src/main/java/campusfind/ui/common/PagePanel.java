package campusfind.ui.common;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/** Base class of every screen: a title, a subtitle and a body area. refresh() reloads the data. */
public class PagePanel extends JPanel {
    private final JPanel body = new JPanel(new BorderLayout(0, 12));

    public PagePanel(String title, String subtitle) {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(Theme.title(title));
        header.add(Box.createVerticalStrut(2));
        header.add(Theme.muted(subtitle));
        body.setOpaque(false);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    protected JPanel body() { return body; }

    /** Called every time the screen is shown. Override to reload data. */
    public void refresh() { }
}
