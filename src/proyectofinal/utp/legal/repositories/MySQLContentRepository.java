package proyectofinal.utp.legal.repositories;

import proyectofinal.utp.legal.composite.*;
import proyectofinal.utp.legal.connetionsdb.ConnetionMYSQL;
import proyectofinal.utp.legal.connetionsdb.IConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLContentRepository implements ContentRepository {

    @Override
    public void saveContent(String documentId, List<DocPart> parts) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection()) {
            con.setAutoCommit(false);

            // 1) limpiar contenido previo
            try (PreparedStatement ps1 = con.prepareStatement(
                     "DELETE c FROM clausulas c " +
                     "JOIN secciones s ON c.seccion_id = s.id " +
                     "WHERE s.documento_id = ?"
                 );
                 PreparedStatement ps2 = con.prepareStatement(
                     "DELETE FROM secciones WHERE documento_id = ?"
                 )) {
                ps1.setString(1, documentId);
                ps1.executeUpdate();
                ps2.setString(1, documentId);
                ps2.executeUpdate();
            }

            // 2) insertar recursivo
            int orden = 1;
            for (DocPart p : parts) {
                if (p instanceof Seccion) {
                    insertSection(con, documentId, (Seccion) p, null, orden++);
                }
            }

            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saveContent: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    private long insertSection(Connection con, String docId, Seccion sec, Long parentId, int orden) throws SQLException {
        long secId;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO secciones(documento_id, titulo, orden, parent_id) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, docId);
            ps.setString(2, sec.getTitulo());
            ps.setInt(3, orden);
            if (parentId == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, parentId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); secId = rs.getLong(1); }
        }

        int childOrder = 1;
        for (DocPart ch : sec.getHijos()) {
            if (ch instanceof Seccion) {
                insertSection(con, docId, (Seccion) ch, secId, childOrder++);
            } else if (ch instanceof Clausula) {
                insertClause(con, secId, (Clausula) ch, childOrder++);
            }
        }
        return secId;
    }

    private void insertClause(Connection con, long secId, Clausula cl, int orden) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO clausulas(seccion_id, texto, orden) VALUES(?,?,?)")) {
            ps.setLong(1, secId);
            ps.setString(2, cl.getTexto());
            ps.setInt(3, orden);
            ps.executeUpdate();
        }
    }

    @Override
    public List<DocPart> loadContent(String documentId) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection()) {
            List<DocPart> out = new ArrayList<>();
            for (SectionRow r : selectTopSections(con, documentId)) {
                out.add(loadSectionRecursive(con, r));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Error loadContent: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    // --- helpers de carga ----
    private static final class SectionRow {
        long id; String titulo; int orden;
        SectionRow(long id, String titulo, int orden) { this.id = id; this.titulo = titulo; this.orden = orden; }
    }

    private List<SectionRow> selectTopSections(Connection con, String docId) throws SQLException {
        String sql = "SELECT id, titulo, orden FROM secciones " +
                     "WHERE documento_id=? AND parent_id IS NULL ORDER BY orden";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SectionRow> list = new ArrayList<>();
                while (rs.next()) list.add(new SectionRow(rs.getLong("id"), rs.getString("titulo"), rs.getInt("orden")));
                return list;
            }
        }
    }

    private List<SectionRow> selectChildSections(Connection con, long parentId) throws SQLException {
        String sql = "SELECT id, titulo, orden FROM secciones WHERE parent_id=? ORDER BY orden";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<SectionRow> list = new ArrayList<>();
                while (rs.next()) list.add(new SectionRow(rs.getLong("id"), rs.getString("titulo"), rs.getInt("orden")));
                return list;
            }
        }
    }

    private Seccion loadSectionRecursive(Connection con, SectionRow r) throws SQLException {
        Seccion s = new Seccion(r.titulo);
        // subsecciones
        for (SectionRow child : selectChildSections(con, r.id)) {
            s.add(loadSectionRecursive(con, child));
        }
        // clausulas
        String sql = "SELECT texto FROM clausulas WHERE seccion_id=? ORDER BY orden";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, r.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) s.add(new Clausula(rs.getString("texto")));
            }
        }
        return s;
    }
}
