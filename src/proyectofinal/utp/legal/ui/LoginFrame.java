package proyectofinal.utp.legal.ui;

import proyectofinal.utp.legal.security.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);
    private final JButton btnLogin = new JButton("Ingresar");

    private final MySQLUserRepository users = new MySQLUserRepository();

    public LoginFrame() {
        super("Login - Documentos Legales");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 180);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridLayout(2,2,8,8));
        form.add(new JLabel("Usuario:"));
        form.add(txtUser);
        form.add(new JLabel("Clave:"));
        form.add(txtPass);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnLogin);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ingresa usuario y clave", "aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            var opt = users.verifyCredentials(u, p);
            if (opt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Usuario/clave inválido o usuario inactivo", "error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            SessionManager.get().login(opt.get());
            java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
            dispose();
        } catch (HeadlessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
