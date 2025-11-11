package proyectofinal.utp.legal.connetionsdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnetionMYSQL implements IConnection {

    private Connection con;

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/doclegales"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASS = "123456";

    @Override
    public Boolean conectar() {
        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, USER, PASS);
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("[MySQL] Driver no encontrado: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.out.println("[MySQL] Error al conectar: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void desconectar() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException ignore) { }
    }

    @Override
    public Connection getConnection() {
        return con;
    }
}
