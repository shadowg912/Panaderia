package Data_base;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CONEXION {


    Connection connection = null;
    String usuario = "sa";
    String db = "PANADERIA";
    String puerto = "1433";
   //String contrasena = "J3sk0912"; String ip = "Localhost";


    String contrasena = "J3sk0912SQL"; String ip = "100.93.3.29";



    String cadena = "jdbc:sqlserver//" + ip + "," + puerto + "/" + db;

    public Connection establecerconexio() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String cadena = "jdbc:sqlserver://" + ip + ":" + puerto + ";" + "databaseName=" + db + ";" + "encrypt=true" + ";" + "trustServerCertificate=true";
            connection = DriverManager.getConnection(cadena, usuario, contrasena);
            System.out.printf("ok");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "te jodiste manito, algo esta mal, no se que, pero esta mal " + e.toString());
        }
        return connection;
    }
    public void Ejecutarsql(String sql){
        try (Connection connection = establecerconexio();
             PreparedStatement ps=connection.prepareStatement(sql)){
            ps.executeUpdate();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
