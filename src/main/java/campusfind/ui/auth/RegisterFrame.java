package campusfind.ui.auth;

import campusfind.service.AuthService;
import campusfind.ui.common.Theme;
import campusfind.ui.common.Ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterFrame extends JDialog {
    private final AuthService auth = new AuthService();
    private final JTextField name = Theme.field(22);
    private final JTextField roll = Theme.field(22);
    private final JTextField email = Theme.field(22);
    private final JPasswordField password = new JPasswordField(22);
    private final JPasswordField confirm = new JPasswordField(22);

    public RegisterFrame(Window owner) {
        super(owner, "Create student account", ModalityType.APPLICATION_MODAL);
        setSize(480, 440);
        setLocationRelativeTo(owner);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        root.add(Theme.title("Register"), BorderLayout.NORTH);

        JPanel form = new JPanel(new java.awt.GridBagLayout());
        Ui.addRow(form, 0, "Full name", name);
        Ui.addRow(form, 1, "Roll number", roll);
        Ui.addRow(form, 2, "Email", email);
        Ui.addRow(form, 3, "Password", password);
        Ui.addRow(form, 4, "Confirm password", confirm);
        root.add(form, BorderLayout.CENTER);

        JButton create = Theme.primaryButton("Create account");
        JButton cancel = Theme.secondaryButton("Cancel");
        create.addActionListener(e -> Ui.attempt(this, () -> {
            auth.register(name.getText(), roll.getText(), email.getText(),
                    new String(password.getPassword()), new String(confirm.getPassword()));
            Ui.info(this, "Account created. You can log in now.");
            dispose();
        }));
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(create);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(create);
    }
}
