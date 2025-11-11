package proyectofinal.utp.legal.connetionsdb;

import java.sql.Connection;

public interface IConnection {
    Boolean conectar();
    void desconectar();
    Connection getConnection();
}
