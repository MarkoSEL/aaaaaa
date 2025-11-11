package proyectofinal.utp.legal.ui;

import proyectofinal.utp.legal.security.SessionManager;
import proyectofinal.utp.legal.security.User;
import proyectofinal.utp.legal.security.Role;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final JLabel lblStatus = new JLabel(" ");

    public MainFrame() {
        super("Documentos Legales - Principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Menu
        setJMenuBar(buildMenuBar());

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Documentos", new DocumentosPanel());
        tabs.addTab("Contenido", new ContenidoPanel());
        tabs.addTab("Admin",     new AdminPanel()); // se agregará y luego se oculta si no es admin

        // ocultar Admin para no-admin
        Role role = Role.USER;
        try {
            User cu = SessionManager.get().currentUser();
            if (cu != null) role = cu.getRole();
        } catch (Exception ignored) {}
        if (role != Role.ADMIN) {
            int idx = tabs.indexOfTab("Admin");
            if (idx >= 0) tabs.removeTabAt(idx);
        }

        add(tabs, BorderLayout.CENTER);

        // Status bar
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        status.add(lblStatus, BorderLayout.WEST);
        add(status, BorderLayout.SOUTH);

        updateStatus();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mArchivo = new JMenu("Archivo");
        JMenuItem miLogout = new JMenuItem("Cerrar sesion");
        JMenuItem miSalir  = new JMenuItem("Salir");

        miLogout.addActionListener(e -> {
            SessionManager.get().logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        miSalir.addActionListener(e -> System.exit(0));

        mArchivo.add(miLogout);
        mArchivo.addSeparator();
        mArchivo.add(miSalir);

        bar.add(mArchivo);
        return bar;
    }

    private void updateStatus() {
        User u = SessionManager.get().currentUser();
        if (u != null) {
            lblStatus.setText("Sesion: " + u.getUsername() + " | Rol: " + u.getRole());
        } else {
            lblStatus.setText("Sin sesion");
        }
    }
}
