package campusfind.ui.student;

import campusfind.model.Item;
import campusfind.service.ClaimResult;
import campusfind.service.ClaimService;
import campusfind.service.ServiceException;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import campusfind.util.AppConfig;
import campusfind.util.SessionManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Ownership verification: the claimant must type the confidential detail of the found item. */
public class ClaimDialog extends JDialog {
    private final ClaimService claims = new ClaimService();
    private final Item item;
    private final JTextField answer = Theme.field(26);
    private final JTextArea notes = new JTextArea(3, 26);
    private final JLabel attempts = new JLabel();
    private final JButton submit = Theme.primaryButton("Verify and submit claim");

    public ClaimDialog(Window owner, Item item) {
        super(owner, "Claim item", ModalityType.APPLICATION_MODAL);
        this.item = item;
        setSize(520, 520);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        root.add(new JLabel("<html><span style='font-size:16px'><b>" + item.title() + "</b></span><br>"
                + item.category() + " | " + item.color() + " | found at " + item.location()
                + " on " + item.eventDate() + "</html>"), BorderLayout.NORTH);

        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        JLabel answerLabel = new JLabel("Confidential detail");
        answerLabel.setFont(Theme.BODY_BOLD);
        JLabel notesLabel = new JLabel("Extra proof (optional)");
        notesLabel.setFont(Theme.BODY_BOLD);
        JPanel answerBox = new JPanel(new BorderLayout(0, 4));
        answerBox.add(answerLabel, BorderLayout.NORTH);
        answerBox.add(answer, BorderLayout.CENTER);
        JPanel notesBox = new JPanel(new BorderLayout(0, 4));
        notesBox.add(notesLabel, BorderLayout.NORTH);
        notesBox.add(new JScrollPane(notes), BorderLayout.CENTER);
        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.add(answerBox, BorderLayout.NORTH);
        form.add(notesBox, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(Theme.muted("<html>Enter the detail only the real owner would know.<br>"
                + "You get " + AppConfig.MAX_FAILED_ATTEMPTS + " failed attempts per item,<br>then you are blocked from this item.</html>"),
                BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        attempts.setFont(Theme.BODY_BOLD);
        center.add(attempts, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JButton cancel = Theme.secondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> submit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(submit);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(submit);
        showAttempts(claims.attemptsLeft(SessionManager.current().id(), item.id()));
    }

    private void showAttempts(int left) {
        if (left <= 0) {
            attempts.setText(ClaimService.LOCKED_MESSAGE);
            attempts.setForeground(Theme.DANGER);
            submit.setEnabled(false);
            answer.setEnabled(false);
        } else {
            attempts.setText("Attempts left for this item: " + left);
            attempts.setForeground(Theme.TEXT);
        }
    }

    private void submit() {
        try {
            ClaimResult r = claims.submitClaim(SessionManager.current(), item.id(), answer.getText(), notes.getText());
            switch (r.outcome()) {
                case VERIFIED -> {
                    Ui.info(this, r.message());
                    dispose();
                }
                case WRONG_ANSWER -> {
                    Ui.error(this, r.message());
                    answer.setText("");
                    showAttempts(r.attemptsLeft());
                }
                case LOCKED -> {
                    Ui.error(this, r.message());
                    showAttempts(0);
                }
            }
        } catch (ServiceException e) {
            Ui.error(this, e.getMessage());
        }
    }
}
