package proyectofinal.utp.legal.security;

import proyectofinal.utp.legal.connetionsdb.ConnetionMYSQL;
import proyectofinal.utp.legal.connetionsdb.IConnection;

import java.sql.*;
import java.util.*;

public class MySQLUserRepository implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT username, Role, active FROM users WHERE username=?")) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new User(
                    rs.getString("username"),
                    Role.valueOf(rs.getString("Role")),
                    rs.getBoolean("active")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error findByUsername: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public Optional<User> verifyCredentials(String username, String rawPassword) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT username, Role, password_hash, active FROM users WHERE username=?")) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                String dbHash = rs.getString("password_hash");
                boolean active = rs.getBoolean("active");
                String calc = PasswordHasher.hash(username, rawPassword);

                if (active && dbHash != null && dbHash.equalsIgnoreCase(calc)) {
                    return Optional.of(new User(
                        rs.getString("username"),
                        Role.valueOf(rs.getString("Role")),
                        true
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("error verifyCredentials: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public List<User> listAll() {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT username, Role, active FROM users ORDER BY username");
             ResultSet rs = ps.executeQuery()) {

            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new User(
                    rs.getString("username"),
                    Role.valueOf(rs.getString("Role")),
                    rs.getBoolean("active")
                ));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("error listAll: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public void createUser(String username, Role Role, String rawPassword) {
        createUser(username, Role, rawPassword, true);
    }

    public void createUser(String username, Role Role, String rawPassword, boolean active) {
        String hash = PasswordHasher.hash(username, rawPassword);
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO users(username, Role, password_hash, active) VALUES(?,?,?,?)")) {
            ps.setString(1, username);
            ps.setString(2, Role.name());
            ps.setString(3, hash);
            ps.setBoolean(4, active);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new RuntimeException("usuario ya existe");
            throw new RuntimeException("error createUser: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public void updateRole(String username, Role Role) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE users SET Role=? WHERE username=?")) {
            ps.setString(1, Role.name());
            ps.setString(2, username);
            if (ps.executeUpdate() == 0) throw new RuntimeException("usuario no encontrado");
        } catch (SQLException e) {
            throw new RuntimeException("error updateRole: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public void updateActive(String username, boolean active) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE users SET active=? WHERE username=?")) {
            ps.setBoolean(1, active);
            ps.setString(2, username);
            if (ps.executeUpdate() == 0) throw new RuntimeException("usuario no encontrado");
        } catch (SQLException e) {
            throw new RuntimeException("error updateActive: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public void resetPassword(String username, String newPassword) {
        String hash = PasswordHasher.hash(username, newPassword);
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE users SET password_hash=? WHERE usuario=?")) {
            ps.setString(1, hash);
            ps.setString(2, username);
            if (ps.executeUpdate() == 0) throw new RuntimeException("usuario no encontrado");
        } catch (SQLException e) {
            throw new RuntimeException("error resetPassword: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }

    public void deleteUser(String username) {
        IConnection cx = new ConnetionMYSQL();
        if (!cx.conectar()) throw new RuntimeException("No hay conexion MySQL");
        try (Connection con = cx.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM users WHERE username=?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("error deleteUser: " + e.getMessage(), e);
        } finally {
            cx.desconectar();
        }
    }
}
