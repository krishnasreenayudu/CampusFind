package campusfind.ui.common;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Dashboard tile: a small caption and a big number. */
public class StatCard extends JPanel {
    private final JLabel value = new JLabel("0");

    public StatCard(String caption) {
        super(new BorderLayout(0, 6));
        setBackground(java.awt.Color.WHITE);
        setBorder(Theme.card().getBorder());
        value.setFont(Theme.H1);
        value.setForeground(Theme.PRIMARY);
        add(Theme.muted(caption), BorderLayout.NORTH);
        add(value, BorderLayout.CENTER);
    }

    public void setValue(Object v) { value.setText(String.valueOf(v)); }
}
