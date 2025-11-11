package proyectofinal.utp.legal.ui;

import proyectofinal.utp.legal.security.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminPanel extends JPanel {

    private final MySQLUserRepository repo = new MySQLUserRepository();

    private final DefaultTableModel model =
        new DefaultTableModel(new Object[]{"Usuario","Rol","Activo"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
    private final JTable tbl = new JTable(model);

    private final JTextField txtUser = new JTextField(14);
    private final JPasswordField txtPass = new JPasswordField(14);
    private final JComboBox<Role> cbRole = new JComboBox<>(Role.values());

    private final JButton btnCrear     = new JButton("Crear");
    private final JButton btnCambRol   = new JButton("Cambiar rol");
    private final JButton btnReset     = new JButton("Reset clave");
    private final JButton btnToggle    = new JButton("Activar/Desactivar"); // nuevo
    private final JButton btnEliminar  = new JButton("Eliminar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    public AdminPanel() {
        super(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Usuario:")); top.add(txtUser);
        top.add(new JLabel("Clave:"));   top.add(txtPass);
        top.add(new JLabel("Rol:"));     top.add(cbRole);
        top.add(btnCrear); top.add(btnCambRol); top.add(btnReset);
        top.add(btnToggle); top.add(btnEliminar); top.add(btnRefrescar);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(tbl), BorderLayout.CENTER);

        boolean isAdmin = SessionManager.get().currentUser().getRole() == Role.ADMIN;
        setEnabledRecursive(this, isAdmin);
        if (!isAdmin) top.add(new JLabel(" Solo ADMIN"), FlowLayout.RIGHT);

        // listeners
        btnRefrescar.addActionListener(e -> load());
        btnCrear.addActionListener(e -> onCrear());
        btnCambRol.addActionListener(e -> onCambiarRol());
        btnReset.addActionListener(e -> onResetClave());
        btnToggle.addActionListener(e -> onToggle());     // nuevo
        btnEliminar.addActionListener(e -> onEliminar());

        tbl.getSelectionModel().addListSelectionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r >= 0) {
                txtUser.setText((String) model.getValueAt(r, 0));
                cbRole.setSelectedItem(Role.valueOf((String) model.getValueAt(r, 1)));
                updateToggleLabel();                       // actualizar texto del boton
            }
        });

        load();
        updateToggleLabel();
    }

    private void setEnabledRecursive(Component c, boolean enabled) {
        if (c != this) c.setEnabled(enabled);
        if (c instanceof Container) {
            for (Component cc : ((Container)c).getComponents()) setEnabledRecursive(cc, enabled);
        }
    }

    private void load() {
        model.setRowCount(0);
        for (User u : repo.listAll()) {
            model.addRow(new Object[]{
                u.getUsername(), u.getRole().name(), u.isActive() ? "1" : "0"
            });
        }
    }

    // ---- helpers de estado activo en la tabla
    private boolean rowActive(int r) {
        Object v = model.getValueAt(r, 2);
        String s = String.valueOf(v);
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "SI".equalsIgnoreCase(s);
    }
    private void updateToggleLabel() {
        int r = tbl.getSelectedRow();
        if (r >= 0) {
            btnToggle.setText(rowActive(r) ? "Desactivar" : "Activar");
        } else {
            btnToggle.setText("Activar/Desactivar");
        }
    }

    private void onCrear() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        Role r = (Role) cbRole.getSelectedItem();
        if (u.isEmpty() || p.isEmpty()) { msg("ingresa usuario y clave"); return; }
        try {
            repo.createUser(u, r, p, true);
            msg("usuario creado");
            txtPass.setText("");
            load();
            selectUser(u);
        } catch (Exception ex) { err(ex); }
    }

    private void onCambiarRol() {
        String u = txtUser.getText().trim();
        Role r = (Role) cbRole.getSelectedItem();
        if (u.isEmpty()) { msg("selecciona usuario"); return; }
        try {
            repo.updateRole(u, r);
            msg("rol actualizado");
            load();
            selectUser(u);
        } catch (Exception ex) { err(ex); }
    }

    private void onResetClave() {
        String u = txtUser.getText().trim();
        if (u.isEmpty()) { msg("selecciona usuario"); return; }
        String p = JOptionPane.showInputDialog(this, "nueva clave:", "reset clave", JOptionPane.QUESTION_MESSAGE);
        if (p == null || p.isEmpty()) return;
        try {
            repo.resetPassword(u, p);
            msg("clave actualizada");
        } catch (Exception ex) { err(ex); }
    }

    private void onToggle() {
        int r = tbl.getSelectedRow();
        if (r < 0) { msg("selecciona usuario"); return; }
        String u = (String) model.getValueAt(r, 0);

        // opcional: evitar desactivar tu propia cuenta
        if (u.equals(SessionManager.get().currentUser().getUsername())) {
            msg("no puedes cambiar el estado de tu propia cuenta");
            return;
        }

        boolean active = rowActive(r);
        boolean newActive = !active;
        try {
            repo.updateActive(u, newActive);
            msg(newActive ? "usuario activado" : "usuario desactivado");
            load();
            selectUser(u);
            updateToggleLabel();
        } catch (Exception ex) { err(ex); }
    }

    private void onEliminar() {
        String u = txtUser.getText().trim();
        if (u.isEmpty()) { msg("selecciona usuario"); return; }
        if (JOptionPane.showConfirmDialog(this, "Eliminar " + u + "?", "confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            repo.deleteUser(u);
            msg("usuario eliminado");
            load();
        } catch (Exception ex) { err(ex); }
    }

    private void selectUser(String username) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (username.equals(model.getValueAt(i, 0))) {
                tbl.setRowSelectionInterval(i, i);
                tbl.scrollRectToVisible(tbl.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private void msg(String s){ JOptionPane.showMessageDialog(this, s, "info", JOptionPane.INFORMATION_MESSAGE); }
    private void err(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(), "error", JOptionPane.ERROR_MESSAGE); }
}