package proyectofinal.utp.legal.ui;

import proyectofinal.utp.legal.app.AppContext;
import proyectofinal.utp.legal.composite.Clausula;
import proyectofinal.utp.legal.composite.DocPart;
import proyectofinal.utp.legal.composite.Seccion;
import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.facade.DocumentUseCases;
import proyectofinal.utp.legal.security.SessionManager;
import proyectofinal.utp.legal.security.Role;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContenidoPanel extends JPanel {

    // top: seleccion de documento + refrescar
    private final JComboBox<Documento> cbDoc = new JComboBox<>();
    private final JButton btnRefrescarDocs = new JButton("Refrescar docs");

    // izquierda: secciones
    private final DefaultListModel<Seccion> modelSecciones = new DefaultListModel<>();
    private final JList<Seccion> lstSecciones = new JList<>(modelSecciones);
    private final JTextField txtSecTitulo = new JTextField(18);
    private final JButton btnAddSec = new JButton("Agregar seccion");
    private final JButton btnDelSec = new JButton("Eliminar seccion");

    // derecha: clausulas de la seccion seleccionada
    private final DefaultListModel<Clausula> modelClausulas = new DefaultListModel<>();
    private final JList<Clausula> lstClausulas = new JList<>(modelClausulas);
    private final JTextArea txtClausula = new JTextArea(4, 22);
    private final JButton btnAddCla = new JButton("Agregar clausula");
    private final JButton btnDelCla = new JButton("Eliminar clausula");

    // abajo: acciones BD y preview
    private final JButton btnGuardar = new JButton("Guardar contenido");
    private final JButton btnCargar  = new JButton("Cargar contenido");
    private final JTextArea txtPreview = new JTextArea(8, 80);

    public ContenidoPanel() {
        super(new BorderLayout(8,8));

        // --- Top (documento)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        cbDoc.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Documento) {
                    Documento d = (Documento) value;
                    String id = d.getId() == null ? "-" : d.getId();
                    String idShort = id.length() > 12 ? id.substring(0, 12) + "..." : id;
                    setText(d.getTitulo() + "  [" + d.getTipo() + " | " + d.getEstado() + "]  {" + idShort + "}");
                }
                return this;
            }
        });
        top.add(new JLabel("Documento:"));
        top.add(cbDoc);
        top.add(btnRefrescarDocs);
        add(top, BorderLayout.NORTH);

        // --- Center: 2 columnas (secciones / clausulas)
        JPanel center = new JPanel(new GridLayout(1,2,8,8));

        // panel secciones
        JPanel pSec = new JPanel(new BorderLayout(6,6));
        pSec.setBorder(BorderFactory.createTitledBorder("Secciones"));
        lstSecciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(pSec);
        pSec.add(new JScrollPane(lstSecciones), BorderLayout.CENTER);

        JPanel pSecBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pSecBottom.add(new JLabel("Titulo:"));
        pSecBottom.add(txtSecTitulo);
        pSecBottom.add(btnAddSec);
        pSecBottom.add(btnDelSec);
        pSec.add(pSecBottom, BorderLayout.SOUTH);

        // panel clausulas
        JPanel pCla = new JPanel(new BorderLayout(6,6));
        pCla.setBorder(BorderFactory.createTitledBorder("Clausulas de la seccion"));
        lstClausulas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        center.add(pCla);
        pCla.add(new JScrollPane(lstClausulas), BorderLayout.CENTER);

        JPanel pClaBottom = new JPanel(new BorderLayout(6,6));
        pClaBottom.add(new JLabel("Texto:"), BorderLayout.NORTH);
        txtClausula.setLineWrap(true);
        txtClausula.setWrapStyleWord(true);
        pClaBottom.add(new JScrollPane(txtClausula), BorderLayout.CENTER);
        JPanel pClaBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pClaBtns.add(btnAddCla);
        pClaBtns.add(btnDelCla);
        pClaBottom.add(pClaBtns, BorderLayout.SOUTH);
        pCla.add(pClaBottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // renderers amigables para listas
        installListRenderers();

        // --- South: acciones BD + preview
        JPanel south = new JPanel(new BorderLayout(6,6));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnGuardar);
        actions.add(btnCargar);
        south.add(actions, BorderLayout.NORTH);

        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPreview.setLineWrap(true);
        txtPreview.setWrapStyleWord(true);
        south.add(new JScrollPane(txtPreview), BorderLayout.CENTER);

        add(south, BorderLayout.SOUTH);

        // listeners
        btnRefrescarDocs.addActionListener(e -> loadDocuments());
        cbDoc.addActionListener(e -> clearWorkingArea());
        lstSecciones.addListSelectionListener(e -> refreshClausulasOfSelected());
        btnAddSec.addActionListener(e -> onAddSeccion());
        btnDelSec.addActionListener(e -> onDelSeccion());
        btnAddCla.addActionListener(e -> onAddClausula());
        btnDelCla.addActionListener(e -> onDelClausula());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCargar.addActionListener(e -> onCargar());

        // primera carga
        loadDocuments();

        // === políticas por rol (USER solo lectura) ===
        Role role = Role.USER;
        try {
            var cu = SessionManager.get().currentUser();
            if (cu != null) role = cu.getRole();
        } catch (Exception ignored) {}
        applyRolePolicies(role);
    }

    // ----- Renderers: muestran títulos/textos en listas -----
    private void installListRenderers() {
        // Secciones: muestra el título
        lstSecciones.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Seccion) {
                    String t;
                    try { t = ((Seccion) value).getTitulo(); } catch (Exception ex) { t = value.toString(); }
                    setText((t == null || t.isBlank()) ? "(sin título)" : t);
                }
                return this;
            }
        });

        // Cláusulas: primera línea / 120 chars y tooltip con el texto completo
        lstClausulas.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Clausula) {
                    String txt;
                    try { txt = ((Clausula) value).getTexto(); } catch (Exception ex) { txt = value.toString(); }
                    if (txt == null) txt = "";
                    String oneLine = txt.replace('\n',' ').trim();
                    String shorty  = oneLine.length() > 120 ? oneLine.substring(0,120) + "…" : oneLine;
                    setText(shorty.isEmpty() ? "(cláusula vacía)" : shorty);
                    setToolTipText(oneLine.isEmpty() ? null : oneLine);
                }
                return this;
            }
        });
    }

    // ======== Políticas por rol: USER = solo lectura =========
    private void applyRolePolicies(Role role) {
        boolean puedeEditar = (role == Role.ADMIN || role == Role.LAWYER);

        // edición
        btnAddSec.setEnabled(puedeEditar);
        btnDelSec.setEnabled(puedeEditar);
        btnAddCla.setEnabled(puedeEditar);
        btnDelCla.setEnabled(puedeEditar);
        btnGuardar.setEnabled(puedeEditar);

        // lectura siempre permitida
        btnCargar.setEnabled(true);
        txtSecTitulo.setEditable(puedeEditar);
        txtClausula.setEditable(puedeEditar);
        lstSecciones.setEnabled(true);

        // visor consolidado: siempre solo lectura
        txtPreview.setEditable(false);
    }

    // -------- helpers de servicio/repos --------
    private DocumentUseCases svcDoc() {
        return SessionManager.get().securedService();
    }
    private proyectofinal.utp.legal.repositories.ContentRepository repoContent() {
        return AppContext.getInstance().content();
    }

    // -------- UI actions --------
    private void loadDocuments() {
        try {
            var docs = svcDoc().listar();
            DefaultComboBoxModel<Documento> m = new DefaultComboBoxModel<>();
            for (Documento d : docs) m.addElement(d);
            cbDoc.setModel(m);
            if (docs.size() > 0) cbDoc.setSelectedIndex(0);
            clearWorkingArea();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void clearWorkingArea() {
        modelSecciones.clear();
        modelClausulas.clear();
        txtSecTitulo.setText("");
        txtClausula.setText("");
        txtPreview.setText("");
    }

    private void refreshClausulasOfSelected() {
        modelClausulas.clear();
        Seccion s = lstSecciones.getSelectedValue();
        if (s == null) return;
        for (DocPart p : s.getHijos()) {
            if (p instanceof Clausula) modelClausulas.addElement((Clausula) p);
        }
        refreshPreview();
    }

    private void onAddSeccion() {
        String t = txtSecTitulo.getText().trim();
        if (t.isEmpty()) { info("ingresa titulo de seccion"); return; }
        Seccion s = new Seccion(t);
        modelSecciones.addElement(s);
        txtSecTitulo.setText("");
        lstSecciones.setSelectedValue(s, true);
        refreshPreview();
    }

    private void onDelSeccion() {
        int idx = lstSecciones.getSelectedIndex();
        if (idx < 0) { info("selecciona una seccion"); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar la sección seleccionada?", "Confirmar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        modelSecciones.remove(idx);
        modelClausulas.clear();
        refreshPreview();
    }

    private void onAddClausula() {
        Seccion s = lstSecciones.getSelectedValue();
        if (s == null) { info("selecciona una seccion"); return; }
        String txt = txtClausula.getText().trim();
        if (txt.isEmpty()) { info("ingresa texto de clausula"); return; }
        Clausula c = new Clausula(txt);
        s.add(c);
        modelClausulas.addElement(c);
        txtClausula.setText("");
        refreshPreview();
    }

    private void onDelClausula() {
        Seccion s = lstSecciones.getSelectedValue();
        Clausula c = lstClausulas.getSelectedValue();
        if (s == null || c == null) { info("selecciona clausula"); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar la cláusula seleccionada?", "Confirmar",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        // reconstruir hijos de la seccion sin la cláusula
        List<DocPart> keep = new ArrayList<>(s.getHijos());
        keep.remove(c);
        s.getHijos().clear();
        for (DocPart p : keep) s.add(p);

        modelClausulas.removeElement(c);
        refreshPreview();
    }

    private void onGuardar() {
        Documento d = (Documento) cbDoc.getSelectedItem();
        if (d == null) { info("no hay documento seleccionado"); return; }
        try {
            List<DocPart> parts = new ArrayList<>();
            for (int i = 0; i < modelSecciones.size(); i++) {
                parts.add(modelSecciones.get(i));
            }
            repoContent().saveContent(d.getId(), parts);
            info("contenido guardado");
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void onCargar() {
        Documento d = (Documento) cbDoc.getSelectedItem();
        if (d == null) { info("no hay documento seleccionado"); return; }
        try {
            List<DocPart> parts = repoContent().loadContent(d.getId());
            modelSecciones.clear();
            modelClausulas.clear();
            for (DocPart p : parts) {
                if (p instanceof Seccion) modelSecciones.addElement((Seccion) p);
            }
            refreshPreview();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void refreshPreview() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < modelSecciones.size(); i++) {
            Seccion s = modelSecciones.get(i);
            String titulo;
            try { titulo = s.getTitulo(); } catch (Exception ex) { titulo = "Sección " + (i+1); }
            if (titulo == null || titulo.isBlank()) titulo = "Sección " + (i+1);

            sb.append((i + 1)).append(". ").append(titulo).append("\n");

            int k = 1;
            for (DocPart p : s.getHijos()) {
                if (p instanceof Clausula) {
                    String txt;
                    try { txt = ((Clausula) p).getTexto(); } catch (Exception ex) { txt = p.toString(); }
                    if (txt == null) txt = "";
                    sb.append("   ").append(i + 1).append(".").append(k++).append(" ")
                      .append(txt.replace("\r","").trim()).append("\n");
                }
            }
            sb.append("\n");
        }
        txtPreview.setText(sb.toString());
        txtPreview.setCaretPosition(0); // ir al inicio
    }

    private void info(String s){ JOptionPane.showMessageDialog(this, s, "info", JOptionPane.INFORMATION_MESSAGE); }
    private void error(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(), "error", JOptionPane.ERROR_MESSAGE); }
}
