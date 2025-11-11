package proyectofinal.utp.legal.ui;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;
import proyectofinal.utp.legal.facade.DocumentUseCases;
import proyectofinal.utp.legal.observer.DocumentObserver;
import proyectofinal.utp.legal.observer.NotificationCenter;
import proyectofinal.utp.legal.security.SessionManager;
import proyectofinal.utp.legal.security.Role;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentosPanel extends JPanel {

    // Crear
    private final JComboBox<Tipo> cbTipo = new JComboBox<>(Tipo.values());
    private final JTextField txtTitulo = new JTextField(18);
    private final JButton btnCrear = new JButton("Crear");

    // Strategy visible: ordenar/filtrar/buscar
    private final JComboBox<String> cbOrden  = new JComboBox<>(new String[]{
            "Fecha (recientes)", "Tipo y titulo", "Estado"
    });
    private final JComboBox<String> cbFiltro = new JComboBox<>(new String[]{
            "Todos", "PENDIENTE", "APROBADO", "FIRMADO", "ARCHIVADO"
    });
    private final JTextField txtBuscar = new JTextField(12);

    // Lista + acciones
    private final DefaultListModel<Documento> listModel = new DefaultListModel<>();
    private final JList<Documento> lstDocs = new JList<>(listModel);
    private final JButton btnAprobar  = new JButton("Aprobar");
    private final JButton btnFirmar   = new JButton("Firmar");
    private final JButton btnArchivar = new JButton("Archivar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    // Metadatos (persisten en BD)
    private final JCheckBox chkUrgente = new JCheckBox("Urgente");
    private final JComboBox<Documento.Prioridad> cbPrioridad =
            new JComboBox<>(Documento.Prioridad.values());
    private final JTextField txtEtiquetas = new JTextField(14);
    private final JButton btnGuardarMeta = new JButton("Guardar meta");

    private boolean updatingMetaUI = false;

    // Log
    private final JTextArea txtLog = new JTextArea(6, 40);

    public DocumentosPanel() {
        super(new BorderLayout(8,8));

        // ==== Barra superior ====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tipo:"));
        top.add(cbTipo);
        top.add(new JLabel("Titulo:"));
        top.add(txtTitulo);
        top.add(btnCrear);
        top.add(btnRefrescar);

        // Controles Strategy visibles
        top.add(new JLabel("Ordenar:"));
        top.add(cbOrden);
        top.add(new JLabel("Estado:"));
        top.add(cbFiltro);
        top.add(new JLabel("Buscar:"));
        top.add(txtBuscar);

        add(top, BorderLayout.NORTH);

        // ==== Centro: lista + acciones ====
        JPanel center = new JPanel(new BorderLayout(8,8));
        lstDocs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstDocs.setCellRenderer(new DocRenderer());
        lstDocs.setFixedCellHeight(-1); // altura variable
        center.add(new JScrollPane(lstDocs), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(btnAprobar);
        actions.add(btnFirmar);
        actions.add(btnArchivar);
        actions.add(btnEliminar);
        actions.add(new JLabel(" | "));
        actions.add(chkUrgente);
        actions.add(new JLabel("Prioridad:"));
        actions.add(cbPrioridad);
        actions.add(new JLabel("Etiquetas:"));
        actions.add(txtEtiquetas);
        actions.add(btnGuardarMeta);
        center.add(actions, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // ==== Log ====
        txtLog.setEditable(false);
        add(new JScrollPane(txtLog), BorderLayout.SOUTH);

        // Observer para log
        NotificationCenter.getInstance().subscribe(new DocumentObserver() {
            @Override public void onEvent(Documento doc, String event) {
                log("[" + event + "] " + doc.getId() + " | " + doc.getTitulo());
            }
        });

        // Acciones
        btnCrear.addActionListener(e -> onCrear());
        btnRefrescar.addActionListener(e -> refreshList());
        btnAprobar.addActionListener(e -> onAccion("aprobar"));
        btnFirmar.addActionListener(e -> onAccion("firmar"));
        btnArchivar.addActionListener(e -> onAccion("archivar"));
        btnEliminar.addActionListener(e -> onAccion("eliminar"));

        // Strategy: re-filtrar/ordenar al cambiar
        cbOrden.addActionListener(e -> refreshList());
        cbFiltro.addActionListener(e -> refreshList());
        txtBuscar.addActionListener(e -> refreshList()); // Enter en buscar

        // Cargar metadatos en UI al seleccionar
        lstDocs.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Documento d = lstDocs.getSelectedValue();
                updatingMetaUI = true;
                if (d == null) {
                    chkUrgente.setSelected(false);
                    cbPrioridad.setSelectedItem(Documento.Prioridad.MEDIA);
                    txtEtiquetas.setText("");
                } else {
                    chkUrgente.setSelected(d.isUrgente());
                    cbPrioridad.setSelectedItem(d.getPrioridad());
                    txtEtiquetas.setText(d.getEtiquetas()==null? "" : d.getEtiquetas());
                }
                updatingMetaUI = false;
            }
        });

        // Guardar metadatos en BD
        btnGuardarMeta.addActionListener(e -> {
            Documento d = lstDocs.getSelectedValue();
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un documento", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                svc().actualizarMetadatos(
                    d.getId(),
                    chkUrgente.isSelected(),
                    (Documento.Prioridad) cbPrioridad.getSelectedItem(),
                    txtEtiquetas.getText().trim()
                );
                refreshList();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        refreshList();

        // Políticas por rol (incluye metadatos)
        applyRolePolicies(SessionManager.get().currentUser().getRole());
    }

    private void applyRolePolicies(Role role) {
        switch (role) {
            case ADMIN:
                setButtons(true, true, true, true, true);
                setMetaEnabled(true);
                break;
            case LAWYER:
                setButtons(true, true, true, true, false);
                setMetaEnabled(true);
                break;
            case USER:
                setButtons(true, false, false, false, false);
                setMetaEnabled(false);
                break;
        }
    }

    private void setMetaEnabled(boolean enabled) {
        chkUrgente.setEnabled(enabled);
        cbPrioridad.setEnabled(enabled);
        txtEtiquetas.setEnabled(enabled);
        btnGuardarMeta.setEnabled(enabled);
    }

    private void setButtons(boolean crear, boolean aprobar, boolean firmar, boolean archivar, boolean eliminar) {
        btnCrear.setEnabled(crear);
        btnAprobar.setEnabled(aprobar);
        btnFirmar.setEnabled(firmar);
        btnArchivar.setEnabled(archivar);
        btnEliminar.setEnabled(eliminar);
    }

    private DocumentUseCases svc() {
        return SessionManager.get().securedService();
    }

    private void onCrear() {
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un titulo", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Tipo tipo = (Tipo) cbTipo.getSelectedItem();
        try {
            Documento d = svc().crear(tipo, titulo);
            log("Creado: " + d.getId());
            txtTitulo.setText("");
            refreshList();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onAccion(String tipo) {
        Documento sel = lstDocs.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un documento", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            switch (tipo) {
                case "aprobar":  svc().aprobar(sel.getId());  break;
                case "firmar":   svc().firmar(sel.getId());   break;
                case "archivar": svc().archivar(sel.getId()); break;
                case "eliminar": svc().eliminar(sel.getId()); break;
            }
            refreshList();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ==== Strategy: filtro + búsqueda + orden ====
    private void refreshList() {
        try {
            List<Documento> docs = svc().listar();

            // 1) Filtro por estado
            String fe = String.valueOf(cbFiltro.getSelectedItem());
            if (!"Todos".equals(fe)) {
                final String estadoFiltro = fe;
                docs = docs.stream()
                        .filter(d -> key(d.getEstado()).equals(estadoFiltro))
                        .collect(Collectors.toList());
            }

            // 2) Búsqueda en título o id
            String q = txtBuscar.getText().trim().toLowerCase();
            if (!q.isEmpty()) {
                final String qq = q;
                docs = docs.stream()
                        .filter(d -> (d.getTitulo() != null && d.getTitulo().toLowerCase().contains(qq)) ||
                                     (d.getId() != null && d.getId().toLowerCase().contains(qq)))
                        .collect(Collectors.toList());
            }

            // 3) Ordenar (estrategia según selección)
            String ord = String.valueOf(cbOrden.getSelectedItem());
            Comparator<Documento> cmp;
            switch (ord) {
                case "Tipo y titulo":
                    cmp = Comparator
                            .comparing((Documento d) -> key(d.getTipo()))
                            .thenComparing(d -> d.getTitulo() == null ? "" : d.getTitulo().toLowerCase());
                    break;
                case "Estado":
                    cmp = Comparator.comparing(d -> key(d.getEstado()));
                    break;
                default: // "Fecha (recientes)"
                    cmp = Comparator.comparing(
                            Documento::getCreado,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ).reversed();
            }
            docs = docs.stream().sorted(cmp).collect(Collectors.toList());

            // 4) Pintar
            listModel.clear();
            for (Documento d : docs) listModel.addElement(d);
            log("Lista actualizada (" + docs.size() + ")");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void log(String s) { txtLog.append(s + "\n"); }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        log("ERROR: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    }

    private static String key(Object v) {
        if (v == null) return "";
        return (v instanceof Enum<?>)
                ? ((Enum<?>) v).name()
                : String.valueOf(v);
    }

    // ===== Renderer bonito con badges =====
    private static class DocRenderer extends JPanel implements ListCellRenderer<Documento> {
        private final JLabel lbl = new JLabel();
        private static final java.time.format.DateTimeFormatter FMT =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        DocRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            add(lbl, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Documento> list,
                Documento d,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            setOpaque(true);
            lbl.setOpaque(true);

            if (d == null) {
                lbl.setText(" ");
                setColors(list, isSelected);
                return this;
            }

            String titulo   = escape(d.getTitulo());
            String tipoStr  = asText(d.getTipo());
            String estadoStr= asText(d.getEstado());
            String creado   = (d.getCreado() != null) ? d.getCreado().format(FMT) : "-";
            String idCorto  = shortId(d.getId());
            String color    = colorEstado(estadoStr);

            // Badges simples (en base a metadatos del documento)
            StringBuilder badges = new StringBuilder();
            if (d.isUrgente()) {
                badges.append("<span style='background:#ffdada;border:1px solid #e57373;"
                        + "padding:2px 6px;border-radius:10px;color:#b71c1c;margin-left:6px;'>URGENTE</span>");
            }
            badges.append("<span style='background:#eef2ff;border:1px solid #90a4ae;"
                    + "padding:2px 6px;border-radius:10px;color:#37474f;margin-left:6px;'>PRIORIDAD: ")
                  .append(escape(d.getPrioridad()==null? "MEDIA" : d.getPrioridad().name()))
                  .append("</span>");

            String html = "<html>"
                    + "<div style='font-weight:bold;font-size:12pt;margin-bottom:2px;'>"
                    + titulo + " " + badges
                    + "</div>"
                    + "<div style='font-size:10pt;color:#444;'>"
                    + "Tipo: <b>" + tipoStr + "</b>"
                    + " &nbsp;·&nbsp; Estado: <b><span style='color:" + color + ";'>" + estadoStr + "</span></b>"
                    + " &nbsp;·&nbsp; Creado: " + creado
                    + "</div>"
                    + "<div style='font-size:9pt;color:#777;margin-top:2px;'>ID: " + idCorto + "</div>"
                    + "</html>";

            lbl.setText(html);
            setColors(list, isSelected);
            return this;
        }

        private void setColors(JList<?> list, boolean isSelected) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
        }

        private static String shortId(String id) {
            if (id == null) return "-";
            int dash = id.indexOf('-');
            String tipo = dash > 0 ? id.substring(0, dash) : id;
            String tail = id.length() > 6 ? id.substring(id.length()-6) : id;
            return tipo + "…" + tail;
        }

        private static String colorEstado(String estado) {
            switch (estado) {
                case "PENDIENTE": return "#666666";
                case "APROBADO":  return "#c77d00";
                case "FIRMADO":   return "#1565c0";
                case "ARCHIVADO": return "#2e7d32";
                default:          return "#444444";
            }
        }

        private static String asText(Object v) {
            if (v == null) return "-";
            return (v instanceof Enum<?>)
                    ? ((Enum<?>) v).name()
                    : String.valueOf(v);
        }

        private static String escape(String s) {
            if (s == null) return "";
            return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        }
    }
}
