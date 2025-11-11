package proyectofinal.utp.legal.repositories;

import proyectofinal.utp.legal.entities.Documento;
import proyectofinal.utp.legal.entities.Documento.Tipo;
import proyectofinal.utp.legal.connetionsdb.ConnetionMYSQL;
import proyectofinal.utp.legal.connetionsdb.IConnection;
import proyectofinal.utp.legal.state.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MySQLDocumentRepository implements IDocumentRepository {

    private static final String BASE_COLS =
        "id, titulo, tipo, estado, creado_at, urgente, prioridad, etiquetas";

    private Documento map(ResultSet rs) throws SQLException {
        String id     = rs.getString("id");
        String titulo = rs.getString("titulo");
        String tipo   = rs.getString("tipo");
        String estado = rs.getString("estado");
        Timestamp ts  = rs.getTimestamp("creado_at");
        LocalDateTime creado = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        Documento d = new Documento(id, titulo, Tipo.valueOf(tipo), creado);

        switch (estado) {
            case "APROBADO":  d.setState(new ApprovedState(d)); break;
            case "FIRMADO":   d.setState(new SignedState(d));   break;
            case "ARCHIVADO": d.setState(new ArchivedState(d)); break;
            default:          d.setState(new PendingState(d));
        }

        d.setUrgente(rs.getBoolean("urgente"));
        String p = rs.getString("prioridad"); // BAJA|MEDIA|ALTA
        if (p != null) d.setPrioridad(Documento.Prioridad.valueOf(p));
        d.setEtiquetas(rs.getString("etiquetas"));
        return d;
    }

    @Override
    public Documento save(Documento d) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection()) {
            String sql =
              "INSERT INTO documentos(id, titulo, tipo, estado, creado_at, urgente, prioridad, etiquetas) " +
              "VALUES (?,?,?,?,?,?,?,?) " +
              "ON DUPLICATE KEY UPDATE " +
              "  titulo=VALUES(titulo), tipo=VALUES(tipo), estado=VALUES(estado), " +
              "  urgente=VALUES(urgente), prioridad=VALUES(prioridad), etiquetas=VALUES(etiquetas)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, d.getId());
                ps.setString(2, d.getTitulo());
                ps.setString(3, d.getTipo().name());
                ps.setString(4, d.getEstado());
                ps.setTimestamp(5, Timestamp.valueOf(d.getCreado()));
                ps.setBoolean(6, d.isUrgente());
                ps.setString(7, d.getPrioridad().name());
                ps.setString(8, d.getEtiquetas());
                ps.executeUpdate();
            }
            return d;
        } catch (SQLException e) {
            throw new RuntimeException("Error save: " + e.getMessage(), e);
        } finally { cx.desconectar(); }
    }

    @Override
    public Optional<Documento> findById(String id) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
               "SELECT " + BASE_COLS + " FROM documentos WHERE id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error findById: " + e.getMessage(), e);
        } finally { cx.desconectar(); }
    }

    @Override
    public List<Documento> findAll() {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
               "SELECT " + BASE_COLS + " FROM documentos ORDER BY creado_at DESC");
             ResultSet rs = ps.executeQuery()) {
            List<Documento> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Error findAll: " + e.getMessage(), e);
        } finally { cx.desconectar(); }
    }

    @Override
    public void deleteById(String id) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM documentos WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleteById: " + e.getMessage(), e);
        } finally { cx.desconectar(); }
    }

    @Override
    public void actualizarMetadatos(String id, boolean urgente,
                                    Documento.Prioridad prioridad, String etiquetas) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
               "UPDATE documentos SET urgente=?, prioridad=?, etiquetas=? WHERE id=?")) {
            ps.setBoolean(1, urgente);
            ps.setString(2, prioridad.name());
            ps.setString(3, (etiquetas==null || etiquetas.isBlank()) ? null : etiquetas);
            ps.setString(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizarMetadatos: " + e.getMessage(), e);
        } finally { cx.desconectar(); }
    }
}
